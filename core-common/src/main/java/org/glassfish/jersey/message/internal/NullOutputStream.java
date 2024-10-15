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
import java.util.Objects;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * Since JDK 11 is replaced by {@link OutputStream#nullOutputStream()}
 */
@Deprecated(since = "3.1.7", forRemoval = true)
public class NullOutputStream extends OutputStream {

    private volatile boolean isClosed;

    @Override
    public void write(int b) throws IOException {
        checkClosed();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        Objects.checkFromIndexSize(off, len, b.length);
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