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
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;

/**
 * A scanner that recursively scans directories and jar files.
 * Files or jar entries are reported to a {@link ResourceProcessor}.
 *
 * @author Paul Sandoz
 */
public final class FilesScanner extends AbstractResourceFinderAdapter {

    private final File[] files;
    private final boolean recursive;

    private CompositeResourceFinder compositeResourceFinder;

    /**
     * Scan from a set of packages.
     *
     * @param fileNames an array of package names.
     * @param recursive flag indicating whether sub-directories of any directories in the list of
     *                  files should be included in the scanning ({@code true}) or not ({@code false}).
     */
    public FilesScanner(final String[] fileNames, final boolean recursive) {
        this.recursive = recursive;
        this.files = new File[Tokenizer.tokenize(fileNames, Tokenizer.COMMON_DELIMITERS).length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(fileNames[i]);
        }

        init();
    }

    private void processFile(final File f) {
        if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
            try {
                compositeResourceFinder.push(new JarFileScanner(new FileInputStream(f), "", true));
            } catch (final IOException e) {
                // logging might be sufficient in this case
                throw new ResourceFinderException(e);
            }

        } else {
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
                        } else if (next.getName().endsWith(".jar") || next.getName().endsWith(".zip")) {
                            processFile(next);
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
        this.compositeResourceFinder = new CompositeResourceFinder();

        for (final File file : files) {
            processFile(file);
        }
    }
}
