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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.fail;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class ChunkedBodyOutputStreamTest {

    @Test
    public void testBasic() throws IOException {
        AsynchronousBodyInputStream responseBody = new AsynchronousBodyInputStream();
        ChunkedBodyOutputStream chunkedStream = getOutputStream(responseBody, 21);

        String sentBody = TestUtils.generateBody(500);
        byte[] sentBytes = sentBody.getBytes();
        for (byte b : sentBytes) {
            chunkedStream.write(b);
        }

        chunkedStream.close();
        verifyReceivedMessage(sentBody, responseBody);
    }

    @Test
    public void testChunkSize() throws IOException {
        doTestChunkSize(1);
    }

    @Test
    public void testChunkSizeWithArray() throws IOException {
        doTestChunkSize(8);
    }

    private void doTestChunkSize(int batchSize) throws IOException {
        final int chunkSize = 21;
        AsynchronousBodyInputStream responseBody = new AsynchronousBodyInputStream() {

            private boolean receivedLess = false;

            @Override
            synchronized void notifyDataAvailable(ByteBuffer availableData) {
                if (availableData.remaining() > chunkSize) {
                    fail();
                }

                if (availableData.remaining() < chunkSize) {
                    assertFalse(receivedLess);
                    receivedLess = true;
                }

                super.notifyDataAvailable(availableData);
            }
        };

        ChunkedBodyOutputStream chunkedStream = getOutputStream(responseBody, chunkSize);

        String sentBody = TestUtils.generateBody(100);
        byte[] sentBytes = sentBody.getBytes();
        if (batchSize > 1) {
            for (int i = 0; i < sentBytes.length; i += 8) {
                chunkedStream.write(sentBytes, i, Math.min(sentBytes.length - i, 8));
            }
        } else {
            for (byte b : sentBytes) {
                chunkedStream.write(b);
            }
        }

        chunkedStream.close();
        verifyReceivedMessage(sentBody, responseBody);
    }

    private ChunkedBodyOutputStream getOutputStream(AsynchronousBodyInputStream responseBody, int chunkSize) {
        ChunkedBodyOutputStream chunkedStream = new ChunkedBodyOutputStream(chunkSize);
        Filter<ByteBuffer, ?, ?, ?> mockTransportFilter = createMockTransportFilter(responseBody);
        chunkedStream.open(mockTransportFilter);
        return chunkedStream;
    }

    private void verifyReceivedMessage(String sentBody, AsynchronousBodyInputStream responseBody) throws IOException {
        byte[] sentBytes = sentBody.getBytes();
        byte[] receivedBytes = new byte[sentBytes.length];

        for (int i = 0; i < sentBytes.length; i++) {
            int b = responseBody.tryRead();
            if (b == -1) {
                fail();
            }

            receivedBytes[i] = (byte) b;
        }

        if (responseBody.tryRead() != -1) {
            fail();
        }

        String receivedBody = new String(receivedBytes);
        assertEquals(sentBody, receivedBody);
    }

    Filter<ByteBuffer, ?, ?, ?> createMockTransportFilter(final AsynchronousBodyInputStream responseBody) {
        HttpParser parser = new HttpParser(Integer.MAX_VALUE, Integer.MAX_VALUE);
        parser.reset(true);
        final TransferEncodingParser transferEncodingParser = TransferEncodingParser
                .createChunkParser(responseBody, parser, 1000);
        return new Filter<ByteBuffer, Void, Void, Void>(null) {

            @Override
            public void write(ByteBuffer chunk, CompletionHandler<ByteBuffer> completionHandler) {
                try {
                    if (transferEncodingParser.parse(chunk)) {
                        responseBody.notifyAllDataRead();
                    }

                    completionHandler.completed(chunk);
                } catch (ParseException e) {
                    completionHandler.failed(e);
                }
            }
        };
    }
}
