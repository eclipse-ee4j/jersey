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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;

/**
 * A "file" scheme URI scanner that recursively scans directories.
 * Files are reported to a {@link ResourceProcessor}.
 *
 * @author Paul Sandoz
 */
final class FileSchemeResourceFinderFactory implements UriSchemeResourceFinderFactory {

    private static final Set<String> SCHEMES = Collections.singleton("file");

    @Override
    public Set<String> getSchemes() {
        return SCHEMES;
    }

    /**
     * Create new "file" scheme URI scanner factory.
     */
    FileSchemeResourceFinderFactory() {
    }

    @Override
    public FileSchemeScanner create(final URI uri, final boolean recursive) {
        return new FileSchemeScanner(uri, recursive);
    }

    private class FileSchemeScanner extends AbstractResourceFinderAdapter {

        private final CompositeResourceFinder compositeResourceFinder;
        private final boolean recursive;

        private FileSchemeScanner(final URI uri, final boolean recursive) {
            this.compositeResourceFinder = new CompositeResourceFinder();
            this.recursive = recursive;

            processFile(new File(uri.getPath()));
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
            throw new UnsupportedOperationException();
        }

        private void processFile(final File f) {
            compositeResourceFinder.push(new AbstractResourceFinderAdapter() {

                Stack<File> files = new Stack<File>() {{
                    if (f.isDirectory()) {
                        final File[] subDirFiles = f.listFiles();
                        if (subDirFiles != null) {
                            for (final File file : subDirFiles) {
                                push(file);
                            }
                        }
                    } else {
                        push(f);
                    }
                }};

                private File current;
                private File next;

                @Override
                public boolean hasNext() {
                    while (next == null && !files.empty()) {
                        next = files.pop();

                        if (next.isDirectory()) {
                            if (recursive) {
                                processFile(next);
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
                        return current.getName();
                    }

                    throw new NoSuchElementException();
                }

                @Override
                public InputStream open() {
                    try {
                        return new FileInputStream(current);
                    } catch (final FileNotFoundException e) {
                        throw new ResourceFinderException(e);
                    }
                }

                @Override
                public void reset() {
                    throw new UnsupportedOperationException();
                }
            });
        }
    }
}
