/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * A {@code "dev/null"} output stream - an output stream implementation that discards all the
 * data written to it. This implementation is not thread-safe.
 *
 * Note that once a null output stream instance is {@link #close() closed}, any subsequent attempts
 * to write the data to the closed stream result in an {@link java.io.IOException} being thrown.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class NullOutputStream extends OutputStream {

    private boolean isClosed;

    @Override
    public void write(int b) throws IOException {
        checkClosed();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void flush() throws IOException {
        checkClosed();
    }

    private void checkClosed() throws IOException {
        if (isClosed) {
            throw new IOException(LocalizationMessages.OUTPUT_STREAM_CLOSED());
        }
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
