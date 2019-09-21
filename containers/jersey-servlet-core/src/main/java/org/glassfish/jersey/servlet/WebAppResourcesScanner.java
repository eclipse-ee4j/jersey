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

package org.glassfish.jersey.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletContext;

import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;
import org.glassfish.jersey.server.internal.scanning.JarFileScanner;
import org.glassfish.jersey.server.internal.scanning.ResourceFinderException;
import org.glassfish.jersey.server.internal.scanning.CompositeResourceFinder;

/**
 * A scanner that recursively scans resources within a Web application.
 *
 * @author Paul Sandoz
 */
final class WebAppResourcesScanner extends AbstractResourceFinderAdapter {

    private static final String[] paths = new String[] {"/WEB-INF/lib/", "/WEB-INF/classes/"};

    private final ServletContext sc;
    private CompositeResourceFinder compositeResourceFinder = new CompositeResourceFinder();

    /**
     * Scan from a set of web resource paths.
     * <p/>
     *
     * @param sc {@link ServletContext}.
     */
    WebAppResourcesScanner(final ServletContext sc) {
        this.sc = sc;

        processPaths(paths);
    }

    private void processPaths(final String... paths) {
        for (final String path : paths) {

            final Set<String> resourcePaths = sc.getResourcePaths(path);
            if (resourcePaths == null) {
                break;
            }

            compositeResourceFinder.push(new AbstractResourceFinderAdapter() {

                private final Deque<String> resourcePathsStack = new LinkedList<String>() {

                    private static final long serialVersionUID = 3109256773218160485L;

                    {
                        for (final String resourcePath : resourcePaths) {
                            push(resourcePath);
                        }
                    }
                };

                private String current;
                private String next;

                @Override
                public boolean hasNext() {
                    while (next == null && !resourcePathsStack.isEmpty()) {
                        next = resourcePathsStack.pop();

                        if (next.endsWith("/")) {
                            processPaths(next);
                            next = null;
                        } else if (next.endsWith(".jar")) {
                            try {
                                compositeResourceFinder.push(new JarFileScanner(sc.getResourceAsStream(next), "", true));
                            } catch (final IOException ioe) {
                                throw new ResourceFinderException(ioe);
                            }
                            next = null;
                        }
                    }

                    return next != null;
                }

                @Override
                public String next() {
                    if (next != null || hasNext()) {
                        current = next;
                        next = null;
                        return current;
                    }

                    throw new NoSuchElementException();
                }

                @Override
                public InputStream open() {
                    return sc.getResourceAsStream(current);
                }

                @Override
                public void reset() {
                    throw new UnsupportedOperationException();
                }
            });

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
        compositeResourceFinder = new CompositeResourceFinder();
        processPaths(paths);
    }
}
