/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import org.glassfish.jersey.client.JerseyCompletionStageRxInvoker;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InvocationBuilderRxTest extends JerseyTest {

    private static final int AWAIT_TIME = 2_000;
    private static final String ECHO = "ECHO";
    private CountDownLatch latch;

    @Before
    public void beforeEach() {
        latch = new CountDownLatch(1);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig().register(Resource.class);
    }

    @Path("/")
    public static class Resource {
        @Path("/double")
        @POST
        public String doublePost(String content) {
            return content + content;
        }

        @Path("/single")
        @GET
        public String doublePost() {
            return ECHO;
        }
    }

    public static class TestCallback implements InvocationCallback<String> {
        private final CountDownLatch latch;

        public TestCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void completed(String s) {
            latch.countDown();
        }

        @Override
        public void failed(Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Test
    public void testMethodCallback() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = target("/single").request().rx(JerseyCompletionStageRxInvoker.class)
                .method("GET", new TestCallback(latch));
        latch.await(AWAIT_TIME, TimeUnit.MILLISECONDS);

        Assert.assertEquals(ECHO, future.get());
        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testMethodEntityCallback() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = target("/double").request().rx(JerseyCompletionStageRxInvoker.class)
                .method("POST", Entity.entity(ECHO, MediaType.TEXT_PLAIN), new TestCallback(latch));
        latch.await(AWAIT_TIME, TimeUnit.MILLISECONDS);

        Assert.assertEquals(ECHO + ECHO, future.get());
        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testMethodEntityResponseType() throws InterruptedException, ExecutionException, TimeoutException {
        CompletionStage<Response> stage = target("/double").request().rx()
                .method("POST", Entity.entity(ECHO, MediaType.TEXT_PLAIN), Response.class);

        try (Response response = stage.toCompletableFuture().get()) {
            Assert.assertEquals(ECHO + ECHO, response.readEntity(String.class));
        }
    }

    @Test
    public void testMethodEntityGenericType() throws InterruptedException, ExecutionException, TimeoutException {
        CompletionStage<Response> stage = target("/double").request().rx()
                .method("POST", Entity.entity(ECHO, MediaType.TEXT_PLAIN), new GenericType<Response>(){});

        try (Response response = stage.toCompletableFuture().get()) {
            Assert.assertEquals(ECHO + ECHO, response.readEntity(String.class));
        }
    }
}
