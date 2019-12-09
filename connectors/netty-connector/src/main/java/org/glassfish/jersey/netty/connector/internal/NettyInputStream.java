/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Input stream which servers as Request entity input.
 * <p>
 * Consumes a list of pending {@link ByteBuf}s and processes them on request by Jersey
 */
public class NettyInputStream extends InputStream {

    private final LinkedBlockingDeque<ByteBuf> isList;

    public NettyInputStream(LinkedBlockingDeque<ByteBuf> isList) {
        this.isList = isList;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        ByteBuf take;
        try {
            take = isList.take();
            boolean isReadable = take.isReadable();
            int read = -1;
            if (checkEndOfInputOrError(take)) {
                take.release();
                return -1;
            }

            if (isReadable) {
                int readableBytes = take.readableBytes();
                read = Math.min(readableBytes, len);
                take.readBytes(b, off, read);
                if (read < len) {
                    take.release();
                } else {
                    isList.addFirst(take);
                }
            } else {
                read = 0;
                take.release(); //We don't need `0`
            }

            return read;
        } catch (InterruptedException e) {
            throw new IOException("Interrupted.", e);
        }
    }

    @Override
    public int read() throws IOException {

        ByteBuf take;
        try {
            take = isList.take();
            boolean isReadable = take.isReadable();
            if (checkEndOfInputOrError(take)) {
                take.release();
                return -1;
            }

            if (isReadable) {
                return take.readInt();
            } else {
                take.release(); //We don't need `0`
            }

            return 0;
        } catch (InterruptedException e) {
            throw new IOException("Interrupted.", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (isList != null) {
            while (!isList.isEmpty()) {
                try {
                    isList.take().release();
                } catch (InterruptedException e) {
                    throw new IOException("Interrupted. Potential ByteBuf Leak.", e);
                }
            }
        }
        super.close();
    }

    @Override
    public int available() throws IOException {
        ByteBuf peek = isList.peek();
        if (peek != null && peek.isReadable()) {
            return peek.readableBytes();
        }
        return 0;
    }

    private boolean checkEndOfInputOrError(ByteBuf take) throws IOException {
        return take == Unpooled.EMPTY_BUFFER;
    }
}
