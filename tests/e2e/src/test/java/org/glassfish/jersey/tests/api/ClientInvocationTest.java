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

package org.glassfish.jersey.tests.api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link Invocation} E2E API tests.
 *
 * @author Michal Gajdos
 */
public class ClientInvocationTest extends JerseyTest {

    private static final int INVOCATIONS = 5;

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("/")
    public static class Resource {

        @GET
        public String get() {
            return "OK";
        }

        @POST
        public String post(final String entity) {
            return entity;
        }
    }

    @Test
    public void testMultipleSyncInvokerCalls() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleSyncInvokerCallsAsString() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleSyncInvokerCallsAsGenericType() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get(new GenericType<String>() {}), is("OK"));
        }
    }

    @Test
    public void testMultipleSyncInvokerCallsWithEntity() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity)).readEntity(String.class), is(entity));
        }
    }

    @Test
    public void testMultipleSyncInvokerCallsAsStringWithEntity() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity), String.class), is(entity));
        }
    }

    @Test
    public void testMultipleSyncInvokerCallsAsGenericTypeWithEntity() throws Exception {
        final Invocation.Builder request = target().request();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity), new GenericType<String>() {}), is(entity));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCalls() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get().get().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCallsAsString() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get(String.class).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCallsAsGenericType() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(request.get(new GenericType<String>() {}).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCallsWithEntity() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity)).get().readEntity(String.class), is(entity));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCallsAsStringWithEntity() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity), String.class).get(), is(entity));
        }
    }

    @Test
    public void testMultipleAsyncInvokerCallsAsGenericTypeWithEntity() throws Exception {
        final AsyncInvoker request = target().request().async();

        for (int i = 0; i < INVOCATIONS; i++) {
            final String entity = "Message: " + i;
            assertThat(request.post(Entity.text(entity), new GenericType<String>() {}).get(), is(entity));
        }
    }

    @Test
    public void testMultipleInvocationInvokes() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationInvokesAsString() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationInvokesAsGenericType() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke(new GenericType<String>() {}), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationInvokesWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationInvokesAsStringWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationInvokesAsGenericTypeWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.invoke(new GenericType<String>() {}), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmits() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit().get().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmitsAsString() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit(String.class).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmitsAsGenericType() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit(new GenericType<String>() {}).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleCallbackInvocationSubmits() throws Exception {
        final Invocation invocation = target().request().buildGet();

        for (int i = 0; i < INVOCATIONS; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<String> response = new AtomicReference<>();

            invocation.submit(new InvocationCallback<String>() {
                @Override
                public void completed(final String s) {
                    response.set(s);
                    latch.countDown();
                }

                @Override
                public void failed(final Throwable throwable) {
                    response.set(throwable.getMessage());
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            assertThat(response.get(), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmitsWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit().get().readEntity(String.class), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmitsAsStringWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit(String.class).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleInvocationSubmitsAsGenericTypeWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            assertThat(invocation.submit(new GenericType<String>() {}).get(), is("OK"));
        }
    }

    @Test
    public void testMultipleCallbackInvocationSubmitsWithEntity() throws Exception {
        final Invocation invocation = target().request().buildPost(Entity.text("OK"));

        for (int i = 0; i < INVOCATIONS; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<String> response = new AtomicReference<>();

            invocation.submit(new InvocationCallback<String>() {
                @Override
                public void completed(final String s) {
                    response.set(s);
                    latch.countDown();
                }

                @Override
                public void failed(final Throwable throwable) {
                    response.set(throwable.getMessage());
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            assertThat(response.get(), is("OK"));
        }
    }
}

