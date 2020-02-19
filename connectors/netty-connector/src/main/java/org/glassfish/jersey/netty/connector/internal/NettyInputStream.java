/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

import io.netty.buffer.ByteBuf;

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
    private Throwable cause;

    private final ArrayDeque<ByteBuf> isList;
    private ByteBuf current;
    private ByteBuffer buffer;

    private byte[] ONE_BYTE;
    private boolean reading;

    public NettyInputStream() {
        this.isList = new ArrayDeque<>();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
       if (current == null) {
          buffer = awaitNext();
          if (buffer == null) {
             // assert: end is true
             if (cause == null) {
                return -1;
             }

             throw new IOException(cause);
          }
       }

       int rem = buffer.remaining();
       if (rem < len) {
          len = rem;
       }
       buffer.get(b, off, len);
       if (rem == len) {
          releaseByteBuf();
       }

       return len;
    }

    @Override
    public int read() throws IOException {
       if (ONE_BYTE == null) {
          ONE_BYTE = new byte[1];
       }
       int r = read(ONE_BYTE, 0, 1);
       if (r < 0) {
          return r;
       }

       return ONE_BYTE[0] & 0xff;
    }

    @Override
    public void close() {

        releaseByteBuf();

        cleanup(true);
    }

    private void releaseByteBuf() {
        if (current != null) {
            current.release();
        }

        current = null;
        buffer = null;
    }

    protected synchronized ByteBuffer awaitNext() {
       while (isList.isEmpty()) {
          if (end) {
             return null;
          }

          try {
             reading = true;
             wait();
             reading = false;
          } catch (InterruptedException ie) {
             // waiting uninterruptibly
          }
       }

       current = isList.poll();
       return current.nioBuffer().asReadOnlyBuffer();
    }

    public void complete(Throwable cause) {
       this.cause = cause;
       cleanup(cause != null);
    }

    protected synchronized void cleanup(boolean drain) {
       if (drain) {
          while (!isList.isEmpty()) {
             isList.poll().release();
          }
       }

       end = true;

       if (reading) {
          notifyAll();
       }
    }

    @Override
    public int available() throws IOException {
        return buffer == null ? 0 : buffer.remaining();
    }

    public synchronized void publish(ByteBuf content) {
       if (end || content.nioBuffer().remaining() == 0) {
          content.release();
          return;
       }

       isList.add(content);
       if (reading) {
          notifyAll();
       }
    }

    public void clear() {
        end = false;
        isList.clear();
    }
}
