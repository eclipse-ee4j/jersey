/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server.internal.scanning;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ReflectPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.internal.OsgiRegistry;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;
import org.glassfish.jersey.uri.UriComponent;

/**
 * A scanner that recursively scans URI-based resources present in a set of
 * package names, and  nested package names of that set. (Recursive scanning of
 * nested packages can be disabled using a proper constructor.)
 * <p>
 * The URIs for a package name are obtained, by default, by invoking
 * {@link ClassLoader#getResources(java.lang.String) } with the parameter that
 * is the package name with "." replaced by "/".
 * <p>
 * Each URI is then scanned using a registered {@link UriSchemeResourceFinderFactory} that
 * supports the URI scheme.
 * <p>
 * The following are registered by default.
 * The {@link FileSchemeResourceFinderFactory} for "file" URI schemes.
 * The {@link JarZipSchemeResourceFinderFactory} for "jar" or "zip" URI schemes to jar
 * resources.
 * The {@link VfsSchemeResourceFinderFactory} for the JBoss-based "vfsfile" and "vfszip"
 * URI schemes.
 * <p>
 * Further schemes may be registered by registering an implementation of
 * {@link UriSchemeResourceFinderFactory} in the META-INF/services file whose name is the
 * the fully qualified class name of {@link UriSchemeResourceFinderFactory}.
 * <p>
 * If a URI scheme is not supported a {@link ResourceFinderException} will be thrown
 * and package scanning deployment will fail.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class PackageNamesScanner extends AbstractResourceFinderAdapter {

    private final boolean recursive;
    private final String[] packages;
    private final ClassLoader classloader;
    private final Map<String, UriSchemeResourceFinderFactory> finderFactories;

    private CompositeResourceFinder compositeResourceFinder;

    /**
     * Scan a set of packages using a context {@link ClassLoader}.
     *
     * The {@code recursive} flag determines whether the packages will be scanned recursively
     * together with their nested packages ({@code true}) or if only the specified packages
     * shall be scanned ({@code false}).
     *
     * @param packages  an array of package names.
     * @param recursive if ({@code true} the packages will be scanned recursively together with
     *                  any nested packages, if {@code false} only the explicitly listed packages
     *                  will be scanned.
     */
    public PackageNamesScanner(final String[] packages, final boolean recursive) {
        this(AccessController.doPrivileged(ReflectionHelper.getContextClassLoaderPA()),
                Tokenizer.tokenize(packages, Tokenizer.COMMON_DELIMITERS), recursive);
    }

    /**
     * Scan a set of packages using the provided {@link ClassLoader}.
     *
     * The {@code recursive} flag determines whether the packages will be scanned recursively
     * together with their nested packages ({@code true}) or if only the specified packages
     * shall be scanned ({@code false}).
     *
     * @param classLoader the {@link ClassLoader} to load classes from.
     * @param packages    an array of package names.
     * @param recursive   if ({@code true} the packages will be scanned recursively together with
     *                    any nested packages, if {@code false} only the explicitly listed packages
     *                    will be scanned.
     */
    public PackageNamesScanner(final ClassLoader classLoader, final String[] packages, final boolean recursive) {
        this.recursive = recursive;
        this.packages = packages.clone();
        this.classloader = classLoader;

        this.finderFactories = new HashMap<>();
        add(new JarZipSchemeResourceFinderFactory());
        add(new FileSchemeResourceFinderFactory());
        add(new VfsSchemeResourceFinderFactory());
        add(new BundleSchemeResourceFinderFactory());

        // TODO - Services?
        // for (UriSchemeResourceFinderFactory s : ServiceFinder.find(UriSchemeResourceFinderFactory.class)) {
        //     add(s);
        // }

        final OsgiRegistry osgiRegistry = ReflectionHelper.getOsgiRegistryInstance();
        if (osgiRegistry != null) {
            setResourcesProvider(new PackageNamesScanner.ResourcesProvider() {

                @Override
                public Enumeration<URL> getResources(final String packagePath, final ClassLoader classLoader) throws IOException {
                    return osgiRegistry.getPackageResources(packagePath, classLoader, recursive);
                }
            });
        }

        init();
    }

    private void add(final UriSchemeResourceFinderFactory uriSchemeResourceFinderFactory) {
        for (final String scheme : uriSchemeResourceFinderFactory.getSchemes()) {
            finderFactories.put(scheme.toLowerCase(), uriSchemeResourceFinderFactory);
        }
    }

    @Override
    public boolean hasNext() {
        return compositeResourceFinder.hasNext();
    }

    @Override
    public String next() {
        return compositeResourceFinder.next();
    }

    @Override
    public InputStream open() {
        return compositeResourceFinder.open();
    }

    @Override
    public void close() {
        compositeResourceFinder.close();
    }

    @Override
    public void reset() {
        close();
        init();
    }

    private void init() {
        compositeResourceFinder = new CompositeResourceFinder();

        for (final String p : packages) {
            try {
                final Enumeration<URL> urls =
                        ResourcesProvider.getInstance().getResources(p.replace('.', '/'), classloader);
                while (urls.hasMoreElements()) {
                    try {
                        addResourceFinder(toURI(urls.nextElement()));
                    } catch (final URISyntaxException e) {
                        throw new ResourceFinderException("Error when converting a URL to a URI", e);
                    }
                }
            } catch (final IOException e) {
                throw new ResourceFinderException("IO error when package scanning jar", e);
            }
        }

    }

    /**
     * Find resources with a given name and class loader.
     */
    public abstract static class ResourcesProvider {

        private static volatile ResourcesProvider provider;

        private static ResourcesProvider getInstance() {
            // Double-check idiom for lazy initialization
            ResourcesProvider result = provider;

            if (result == null) { // first check without locking
                synchronized (ResourcesProvider.class) {
                    result = provider;
                    if (result == null) { // second check with locking
                        provider = result = new ResourcesProvider() {

                            @Override
                            public Enumeration<URL> getResources(final String name, final ClassLoader cl)
                                    throws IOException {
                                return cl.getResources(name);
                            }
                        };

                    }
                }

            }
            return result;
        }

        private static void setInstance(final ResourcesProvider provider) throws SecurityException {
            final SecurityManager security = System.getSecurityManager();
            if (security != null) {
                final ReflectPermission rp = new ReflectPermission("suppressAccessChecks");
                security.checkPermission(rp);
            }
            synchronized (ResourcesProvider.class) {
                ResourcesProvider.provider = provider;
            }
        }

        /**
         * Find all resources with the given name using a class loader.
         *
         * @param cl   the class loader use to find the resources
         * @param name the resource name
         * @return An enumeration of URL objects for the resource.
         *         If no resources could be found, the enumeration will be empty.
         *         Resources that the class loader doesn't have access to will
         *         not be in the enumeration.
         * @throws IOException if I/O errors occur
         */
        public abstract Enumeration<URL> getResources(String name, ClassLoader cl) throws IOException;
    }

    /**
     * Set the {@link ResourcesProvider} implementation to find resources.
     * <p>
     * This method should be invoked before any package scanning is performed
     * otherwise the functionality method will be utilized.
     *
     * @param provider the resources provider.
     * @throws SecurityException if the resources provider cannot be set.
     */
    public static void setResourcesProvider(final ResourcesProvider provider) throws SecurityException {
        ResourcesProvider.setInstance(provider);
    }

    private void addResourceFinder(final URI u) {
        final UriSchemeResourceFinderFactory finderFactory = finderFactories.get(u.getScheme().toLowerCase());
        if (finderFactory != null) {
            compositeResourceFinder.push(finderFactory.create(u, recursive));
        } else {
            throw new ResourceFinderException("The URI scheme " + u.getScheme()
                    + " of the URI " + u
                    + " is not supported. Package scanning deployment is not"
                    + " supported for such URIs."
                    + "\nTry using a different deployment mechanism such as"
                    + " explicitly declaring root resource and provider classes"
                    + " using an extension of javax.ws.rs.core.Application");
        }
    }

    private URI toURI(final URL url) throws URISyntaxException {
        try {
            return url.toURI();
        } catch (final URISyntaxException e) {
            // Work around bug where some URLs are incorrectly encoded.
            // This can occur when certain class loaders are utilized
            // to obtain URLs for resources.
            return URI.create(toExternalForm(url));
        }
    }

    private String toExternalForm(final URL u) {

        // pre-compute length of StringBuffer
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            len += 2 + u.getAuthority().length();
        }
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += 1 + u.getQuery().length();
        }
        if (u.getRef() != null) {
            len += 1 + u.getRef().length();
        }

        final StringBuilder result = new StringBuilder(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(UriComponent.contextualEncode(u.getPath(), UriComponent.Type.PATH));
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(UriComponent.contextualEncode(u.getQuery(), UriComponent.Type.QUERY));
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }
}
