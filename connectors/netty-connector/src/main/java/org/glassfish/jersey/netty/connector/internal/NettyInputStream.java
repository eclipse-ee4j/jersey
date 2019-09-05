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

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Input stream which servers as Request entity input.
 * <p>
 * Converts Netty NIO buffers to an input streams and stores them in the queue,
 * waiting for Jersey to process it.
 *
 * @author Pavel Bucek
 */
public class NettyInputStream extends InputStream {

    private volatile boolean end = false;

    /**
     * End of input.
     */
    public static final InputStream END_OF_INPUT = new InputStream() {
        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public String toString() {
            return "END_OF_INPUT " + super.toString();
        }
    };

    /**
     * Unexpected end of input.
     */
    public static final InputStream END_OF_INPUT_ERROR = new InputStream() {
        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public String toString() {
            return "END_OF_INPUT_ERROR " + super.toString();
        }
    };

    private final LinkedBlockingDeque<InputStream> isList;

    private final ChannelHandlerContext ctx;

    public NettyInputStream(LinkedBlockingDeque<InputStream> isList, ChannelHandlerContext ctx) {
        this.isList = isList;
        this.ctx = ctx;
    }

    private interface ISReader {
        int readFrom(InputStream take) throws IOException;
    }

    private int readInternal(ISReader isReader) throws IOException {
        if (end) {
            return -1;
        }

        InputStream take;
        try {
            take = isList.take();

            if (checkEndOfInput(take)) {
                return -1;
            }

            int read = isReader.readFrom(take);

            if (take.available() > 0) {
                isList.addFirst(take);
            } else {
                // Must run on channel's eventloop thread
                ctx.executor().submit(new Runnable() {
                    @Override
                    public void run() {
                        releaseBuffer(take);
                    }
                });
            }

            return read;
        } catch (InterruptedException e) {
            throw new IOException("Interrupted.", e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return readInternal(take -> take.read(b, off, len));
    }

    @Override
    public int read() throws IOException {
        return readInternal(InputStream::read);
    }

    @Override
    public int available() throws IOException {
        InputStream peek = isList.peek();
        if (peek != null) {
            return peek.available();
        }

        return 0;
    }

    private boolean checkEndOfInput(InputStream take) throws IOException {
        if (take == END_OF_INPUT) {
            end = true;
            return true;
        } else if (take == END_OF_INPUT_ERROR) {
            end = true;
            throw new IOException("Connection was closed prematurely.");
        }
        return false;
    }

    private void releaseBuffer(InputStream stream) {
        if (stream instanceof ByteBufInputStream) {
            try {
                stream.close();
            } catch (IOException e) {
                // should not happen
            }
        }
    }

}
