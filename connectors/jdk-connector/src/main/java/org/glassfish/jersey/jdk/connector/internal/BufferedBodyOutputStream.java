/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class BufferedBodyOutputStream extends BodyOutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /* This whole mode stuff is totally pointless if we buffer the request body,
    it is here only in case someone complained that this stream does not behave as BodyOutputStream says it should */
    private volatile Mode mode = Mode.UNDECIDED;

    @Override
    public void setWriteListener(WriteListener writeListener) {
        if (mode == Mode.ASYNCHRONOUS) {
            throw new IllegalStateException(LocalizationMessages.WRITE_LISTENER_SET_ONLY_ONCE());
        }

        if (mode == Mode.SYNCHRONOUS) {
            throw new UnsupportedOperationException(LocalizationMessages.ASYNC_OPERATION_NOT_SUPPORTED());
        }

        mode = Mode.ASYNCHRONOUS;
        try {
            writeListener.onWritePossible();
        } catch (IOException e) {
            writeListener.onError(e);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void write(int b) throws IOException {
        if (mode == Mode.UNDECIDED) {
            mode = Mode.SYNCHRONOUS;
        }

        buffer.write(b);
    }

    ByteBuffer toBuffer() {
        return ByteBuffer.wrap(buffer.toByteArray());
    }

    private enum Mode {
        UNDECIDED,
        ASYNCHRONOUS,
        SYNCHRONOUS
    }
}
