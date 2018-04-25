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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;
import org.glassfish.jersey.uri.UriComponent;

/**
 * A "jar", "zip" and "wsjar" scheme URI scanner that recursively jar files.
 * Jar entries are reported to a {@link ResourceProcessor}.
 *
 * @author Paul Sandoz
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
final class JarZipSchemeResourceFinderFactory implements UriSchemeResourceFinderFactory {

    private static final Set<String> SCHEMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("jar", "zip", "wsjar")));

    @Override
    public Set<String> getSchemes() {
        return SCHEMES;
    }

    /**
     * Create new "jar", "zip" and "wsjar" scheme URI scanner factory.
     */
    JarZipSchemeResourceFinderFactory() {
    }

    @Override
    public JarZipSchemeScanner create(final URI uri, final boolean recursive) {
        final String ssp = uri.getRawSchemeSpecificPart();
        final String jarUrlString = ssp.substring(0, ssp.lastIndexOf('!'));
        final String parent = ssp.substring(ssp.lastIndexOf('!') + 2);

        try {
            return new JarZipSchemeScanner(getInputStream(jarUrlString), parent, recursive);
        } catch (final IOException e) {
            throw new ResourceFinderException(e);
        }
    }

    private class JarZipSchemeScanner extends AbstractResourceFinderAdapter {

        private final InputStream inputStream;
        private final JarFileScanner jarFileScanner;

        private JarZipSchemeScanner(final InputStream inputStream, final String parent, final boolean recursive)
                throws IOException {
            this.inputStream = inputStream;
            this.jarFileScanner = new JarFileScanner(inputStream, parent, recursive);
        }

        @Override
        public boolean hasNext() {
            final boolean hasNext = jarFileScanner.hasNext();
            if (!hasNext) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    Logger.getLogger(JarZipSchemeScanner.class.getName()).log(Level.FINE, "Unable to close jar file.", e);
                }
                return false;
            }

            return true;
        }

        @Override
        public String next() {
            return jarFileScanner.next();
        }

        @Override
        public InputStream open() {
            return jarFileScanner.open();
        }

        @Override
        public void close() {
            jarFileScanner.close();
        }

        @Override
        public void reset() {
            jarFileScanner.reset();
        }
    }

    /**
     * Obtain a {@link InputStream} of the jar file.
     * <p>
     * For most platforms the format for the zip or jar follows the form of
     * the <a href="http://docs.sun.com/source/819-0913/author/jar.html#jarprotocol"jar protcol.</a></p>
     * <ul>
     * <li><code>jar:file:///tmp/fishfingers.zip!/example.txt</code></li>
     * <li><code>zip:http://www.example.com/fishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p>
     * On versions of the WebLogic application server a proprietary format is
     * supported of the following form, which assumes a zip file located on
     * the local file system:
     * </p>
     * <ul>
     * <li><code>zip:/tmp/fishfingers.zip!/example.txt</code></li>
     * <li><code>zip:d:/tempfishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p/>
     * This method will first attempt to create a {@link InputStream} as follows:
     * <pre>
     *   new URL(jarUrlString).openStream();
     * </pre>
     * if that fails with a {@link java.net.MalformedURLException} then the method will
     * attempt to create a {@link InputStream} instance as follows:
     * <pre>
     *  return new new FileInputStream(
     *      UriComponent.decode(jarUrlString, UriComponent.Type.PATH)));
     * </pre>
     *
     * @param jarUrlString the raw scheme specific part of a URI minus the jar
     *                     entry
     * @return a {@link InputStream}.
     * @throws IOException if there is an error opening the stream.
     */
    private InputStream getInputStream(final String jarUrlString) throws IOException {
        try {
            return new URL(jarUrlString).openStream();
        } catch (final MalformedURLException e) {
            return new FileInputStream(
                    UriComponent.decode(jarUrlString, UriComponent.Type.PATH));
        }
    }
}
