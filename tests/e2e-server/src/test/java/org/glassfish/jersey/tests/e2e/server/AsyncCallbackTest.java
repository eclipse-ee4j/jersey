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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ManagedAsync;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ConnectionCallback connection callback}.
 *
 * @author Miroslav Fuksa
 */
public class AsyncCallbackTest extends JerseyTest {

    public static final AtomicBoolean onDisconnectCalled = new AtomicBoolean(false);

    public static CountDownLatch streamClosedSignal;
    public static CountDownLatch callbackCalledSignal;

    @Path("resource")
    public static class Resource {

        @GET
        @ManagedAsync
        @Path("outputStream")
        public void get(@Suspended final AsyncResponse asyncResponse) throws IOException, InterruptedException {
            asyncResponse.register(MyConnectionCallback.class);
            final InputStream is = new InputStream() {
                private int counter = 0;

                @Override
                public int read() throws IOException {
                    return 65 + (++counter % 35);
                }

            };
            asyncResponse.resume(is);
        }

        @GET
        @ManagedAsync
        @Path("chunked")
        public void getChunkedOutput(@Suspended AsyncResponse asyncResponse) throws IOException, InterruptedException {
            asyncResponse.register(MyConnectionCallback.class);
            ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String>(String.class);
            asyncResponse.resume(chunkedOutput);
            for (int i = 0; i < 50000; i++) {
                chunkedOutput.write("something-");
            }
        }
    }

    public static class TestLatch extends CountDownLatch {

        private final String name;
        private final int multiplier;

        public TestLatch(int count, String name, int multiplier) {
            super(count);
            this.name = name;
            this.multiplier = multiplier;
        }

        @Override
        public void countDown() {
            super.countDown();
        }

        @Override
        public void await() throws InterruptedException {
            final boolean success = super.await(10 * multiplier, TimeUnit.SECONDS);
            Assert.assertTrue(
                    Thread.currentThread().getName() + ": Latch [" + name + "] awaiting -> timeout!!!",
                    success);
        }
    }

    @Before
    public void setup() {
        onDisconnectCalled.set(false);
        streamClosedSignal = new TestLatch(1, "streamClosedSignal", getAsyncTimeoutMultiplier());
        callbackCalledSignal = new TestLatch(1, "callbackCalledSignal", getAsyncTimeoutMultiplier());

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testOutputStream() throws InterruptedException, IOException {
        _testConnectionCallback("resource/outputStream");
    }

    @Test
    public void testChunkedOutput() throws InterruptedException, IOException {
        _testConnectionCallback("resource/chunked");
    }

    private void _testConnectionCallback(String path) throws IOException, InterruptedException {
        final Response response = target().path(path).request().get();
        final InputStream inputStream = response.readEntity(InputStream.class);
        for (int i = 0; i < 500; i++) {
            inputStream.read();
        }
        response.close();
        streamClosedSignal.countDown();
        callbackCalledSignal.await();
        Assert.assertTrue(onDisconnectCalled.get());
    }

    public static class MyConnectionCallback implements ConnectionCallback {

        @Override
        public void onDisconnect(AsyncResponse disconnected) {
            onDisconnectCalled.set(true);
            callbackCalledSignal.countDown();
        }
    }
}
