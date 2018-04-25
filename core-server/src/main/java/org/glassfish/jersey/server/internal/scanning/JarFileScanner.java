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
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * A utility class that scans entries in jar files.
 *
 * @author Paul Sandoz
 */
public final class JarFileScanner extends AbstractResourceFinderAdapter {

    private static final Logger LOGGER = Logger.getLogger(JarFileScanner.class.getName());
    // platform independent file separator within the jar file
    private static final char JAR_FILE_SEPARATOR = '/';

    private final JarInputStream jarInputStream;
    private final String parent;
    private final boolean recursive;

    /**
     * Create new JAR file scanner.
     *
     * @param inputStream JAR file input stream
     * @param parent      JAR file entry prefix.
     * @param recursive   if ({@code true} the packages will be scanned recursively together with any nested packages, if
     *                    {@code false} only the explicitly listed packages will be scanned.
     * @throws IOException if wrapping given input stream into {@link JarInputStream} failed.
     */
    public JarFileScanner(final InputStream inputStream, final String parent, final boolean recursive) throws IOException {
        this.jarInputStream = new JarInputStream(inputStream);
        this.parent = (parent.isEmpty() || parent.endsWith(String.valueOf(JAR_FILE_SEPARATOR)))
                ? parent : parent + JAR_FILE_SEPARATOR;
        this.recursive = recursive;
    }

    private JarEntry next = null;

    @Override
    public boolean hasNext() {
        if (next == null) {
            try {
                do {
                    this.next = jarInputStream.getNextJarEntry();
                    if (next == null) {
                        break;
                    }
                    if (!next.isDirectory() && next.getName().startsWith(parent)) {
                        if (recursive || next.getName().substring(parent.length()).indexOf(JAR_FILE_SEPARATOR) == -1) {
                            break;
                        }
                    }
                } while (true);
            } catch (final IOException | SecurityException e) {
                LOGGER.log(Level.CONFIG, LocalizationMessages.JAR_SCANNER_UNABLE_TO_READ_ENTRY(), e);
                return false;
            }
        }

        if (next == null) {
            close();

            return false;
        }

        return true;
    }

    @Override
    public String next() {
        if (next != null || hasNext()) {
            final String name = next.getName();
            next = null;
            return name;
        }

        throw new NoSuchElementException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream open() {
        //noinspection NullableProblems
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return jarInputStream.read();
            }

            @Override
            public int read(final byte[] bytes) throws IOException {
                return jarInputStream.read(bytes);
            }

            @Override
            public int read(final byte[] bytes, final int i, final int i2) throws IOException {
                return jarInputStream.read(bytes, i, i2);
            }

            @Override
            public long skip(final long l) throws IOException {
                return jarInputStream.skip(l);
            }

            @Override
            public int available() throws IOException {
                return jarInputStream.available();
            }

            @Override
            public void close() throws IOException {
                jarInputStream.closeEntry();
            }

            @Override
            public synchronized void mark(final int i) {
                jarInputStream.mark(i);
            }

            @Override
            public synchronized void reset() throws IOException {
                jarInputStream.reset();
            }

            @Override
            public boolean markSupported() {
                return jarInputStream.markSupported();
            }
        };
    }

    @Override
    public void close() {
        try {
            jarInputStream.close();
        } catch (final IOException ioe) {
            LOGGER.log(Level.FINE, LocalizationMessages.JAR_SCANNER_UNABLE_TO_CLOSE_FILE(), ioe);
        }
    }
}

