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

package org.glassfish.jersey.innate.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic wrapper template for InputStream.
 */
public abstract class InputStreamWrapper extends InputStream {

    /**
     * Return the wrapped stream
     * @return
     */
    protected abstract InputStream getWrapped();

    /**
     * Get wrapped stream that can throw {@link IOException}
     * @return the wrapped InputStream.
     * @throws IOException
     */
    protected InputStream getWrappedIOE() throws IOException {
        return getWrapped();
    }

    @Override
    public int read() throws IOException {
        return getWrappedIOE().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getWrappedIOE().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getWrappedIOE().read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return getWrappedIOE().readAllBytes();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return getWrappedIOE().readNBytes(b, off, len);
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return getWrappedIOE().transferTo(out);
    }


    @Override
    public long skip(long n) throws IOException {
        return getWrappedIOE().skip(n);
    }

    @Override
    public int available() throws IOException {
        return getWrappedIOE().available();
    }

    @Override
    public void close() throws IOException {
        getWrappedIOE().close();
    }

    @Override
    public void mark(int readlimit) {
        getWrapped().mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        getWrappedIOE().reset();
    }

    @Override
    public boolean markSupported() {
        return getWrapped().markSupported();
    }
}
