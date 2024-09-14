/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * After Java 9, in the class Inflater the method finalize doesn't call end anymore
 * The method end must be called explicitly. But when using the constructor
 * InflaterInputStream(InputStream out, Inflater def), the Inflater.end() method will
 * not be called when closing the stream.
 * This lead to memory leaks in the off-heap.
 *
 * @see java.util.zip.Inflater
 * @see java.util.zip.InflaterInputStream
 */
public class ClosingInflaterInputStream extends InputStream {

    private final Inflater inflater;

    private final InflaterInputStream delegate;

    private boolean closed = false;

    public ClosingInflaterInputStream(InputStream inputStream, boolean nowrap) {
        inflater = new Inflater(nowrap);
        delegate = new InflaterInputStream(inputStream, inflater);
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            inflater.end();
            delegate.close();
            closed = true;
        }
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

}
