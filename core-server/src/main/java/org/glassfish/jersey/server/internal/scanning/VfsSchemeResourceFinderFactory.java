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
import java.lang.reflect.Method;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;

/**
 * A JBoss-based "vfsfile", "vfs" and "vfszip" scheme URI scanner.
 * <p/>
 * This approach uses reflection to allow for zero-deps and support
 * for both the v2 (EAP5, AS5) and v3 VFS APIs (AS6, AS7, EAP6 & WildFly)
 * which are not binary compatible.
 *
 * @author Jason T. Greene
 * @author Paul Sandoz
 */
final class VfsSchemeResourceFinderFactory implements UriSchemeResourceFinderFactory {

    private static final Set<String> SCHEMES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("vfsfile", "vfszip", "vfs")));

    public Set<String> getSchemes() {
        return SCHEMES;
    }

    VfsSchemeResourceFinderFactory() {
    }

    @Override
    public ResourceFinder create(final URI uri, final boolean recursive) {
        return new VfsResourceFinder(uri, recursive);
    }

    private static class VfsResourceFinder extends AbstractResourceFinderAdapter {

        private Object current;
        private Object next;
        private final Method openStream;
        private final Method getName;
        private final Method isLeaf;
        private final Iterator<?> iterator;

        public VfsResourceFinder(final URI uri, final boolean recursive) {
            final Object directory = bindDirectory(uri);
            this.openStream = bindMethod(directory, "openStream");
            this.getName = bindMethod(directory, "getName");
            this.isLeaf = bindMethod(directory, "isLeaf");
            this.iterator = getChildren(directory, recursive);
        }

        private Iterator<?> getChildren(final Object directory, final boolean recursive) {
            final Method getChildren = bindMethod(directory, recursive ? "getChildrenRecursively" : "getChildren");

            final List<?> list = invoke(directory, getChildren, List.class);
            if (list == null) {
                throw new ResourceFinderException("VFS object returned null when accessing children");
            }

            return list.iterator();
        }

        private Method bindMethod(final Object object, final String name) {
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged(new PrivilegedAction<Method>() {
                    public Method run() {
                        return bindMethod0(object, name);
                    }
                });
            }

            return bindMethod0(object, name);
        }

        private <T> T invoke(final Object instance, final Method method, final Class<T> type) {
            try {
                return type.cast(method.invoke(instance));
            } catch (final Exception e) {
                throw new ResourceFinderException("VFS object could not be invoked upon");
            }
        }

        private Method bindMethod0(final Object object, final String name) {
            final Class<?> clazz = object.getClass();

            try {
                return clazz.getMethod(name);
            } catch (final NoSuchMethodException e) {
                throw new ResourceFinderException("VFS object did not have a valid signature");
            }
        }

        private Object bindDirectory(final URI uri) {
            Object directory = null;
            try {
                directory = uri.toURL().getContent();
            } catch (final IOException e) {
                // Eat
            }

            if (directory == null || !directory.getClass().getSimpleName().equals("VirtualFile")) {
                throw new ResourceFinderException("VFS URL did not map to a valid VFS object");
            }

            return directory;
        }

        @Override
        public InputStream open() {
            final Object current = this.current;
            if (current == null) {
                throw new IllegalStateException("next() must be called before open()");
            }

            return invoke(current, openStream, InputStream.class);
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }

        public boolean advance() {
            while (iterator.hasNext()) {
                final Object next = iterator.next();
                if (invoke(next, isLeaf, Boolean.class)) {
                    this.next = next;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean hasNext() {
            return next != null || advance();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            current = next;
            next = null;
            return invoke(current, getName, String.class);
        }
    }
}
