/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.Broadcaster;
import org.glassfish.jersey.server.BroadcasterListener;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class BroadcasterTest extends JerseyTest {
    static Broadcaster<String> broadcaster = new Broadcaster<String>() {
        @Override
        public void onClose(ChunkedOutput<String> stringChunkedOutput) {
            closedOutputs.add(stringChunkedOutput);
        }
    };

    static List<ChunkedOutput<String>> outputs = new ArrayList<>();
    static List<ChunkedOutput<String>> closedOutputs = new ArrayList<>();
    static int listenerClosed = 0;

    @Path("/test")
    public static class MyResource {
        @GET
        public ChunkedOutput<String> get() {
            ChunkedOutput<String> result = new ChunkedOutput<String>() {};

            // write something to ensure the client does not get blocked on waiting for the first byte
            try {
                result.write("firstChunk");
            } catch (IOException e) {
                e.printStackTrace();
            }

            outputs.add(result);
            broadcaster.add(result);
            return result;
        }

        @POST
        public String post(String text) {
            broadcaster.broadcast(text);
            return text;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    @Test
    public void testBroadcaster() throws IOException {
        InputStream is1 = getChunkStream();
        InputStream is2 = getChunkStream();
        InputStream is3 = getChunkStream();
        InputStream is4 = getChunkStream();

        target("test").request().post(Entity.text("text1"));
        checkClosed(0);
        checkStream("firstChunktext1", is1, is2, is3, is4);

        outputs.remove(0).close();

        target("test").request().post(Entity.text("text2"));
        checkStream("text2", is2, is3, is4);
        checkClosed(1);

        outputs.remove(0).close();

        BroadcasterListener<String> bl = new BroadcasterListener<String>() {
            @Override
            public void onException(ChunkedOutput<String> stringChunkedResponse, Exception exception) {
            }

            @Override
            public void onClose(ChunkedOutput<String> stringChunkedResponse) {
                listenerClosed++;
            }
        };

        broadcaster.add(bl);

        target("test").request().post(Entity.text("text3"));
        checkClosed(2);
        assertEquals(1, listenerClosed);

        broadcaster.remove(bl);
        broadcaster.closeAll();

        checkClosed(4);
        assertEquals(1, listenerClosed);

        checkStream("text3", is3, is4);
    }

    private InputStream getChunkStream() {
        return target("test").request().get(InputStream.class);
    }

    private void checkStream(String golden, InputStream... inputStreams) throws IOException {
        byte[] bytes = golden.getBytes();
        byte[] entity = new byte[bytes.length];
        for (InputStream is : inputStreams) {
            int bytesRead = 0;
            int previous = 0;
            while ((bytesRead += is.read(entity, bytesRead, entity.length - bytesRead)) < entity.length
                    && previous != bytesRead) {
                previous = bytesRead;
            }
            assertEquals(golden, new String(entity));
        }
    }

    private void checkClosed(int count) {
        assertEquals("Closed count does not match", count, closedOutputs.size());
    }
}
