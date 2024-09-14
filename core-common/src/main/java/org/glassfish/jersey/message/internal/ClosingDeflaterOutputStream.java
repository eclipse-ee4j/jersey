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
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * After Java 9, in the class Deflater the method finalize doesn't call end anymore
 * The method end must be called explicitly. But when using the constructor
 * DeflaterOutputStream(OutputStream out, Deflater def), the Deflater.end() method will
 * not be called when closing the stream.
 * This lead to memory leaks in the off-heap.
 *
 * @see java.util.zip.Deflater
 * @see java.util.zip.DeflaterOutputStream
 */
public final class ClosingDeflaterOutputStream extends OutputStream {

    private final Deflater deflater;

    private final DeflaterOutputStream delegate;

    private boolean closed = false;

    public ClosingDeflaterOutputStream(OutputStream out, int level, boolean nowrap) {
        deflater = new Deflater(level, nowrap);
        delegate = new DeflaterOutputStream(out, deflater);
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    public void finish() throws IOException {
        delegate.finish();
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                delegate.close();
            } finally {
                deflater.end();
            }
            closed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

}
