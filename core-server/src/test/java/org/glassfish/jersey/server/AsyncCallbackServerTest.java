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

package org.glassfish.jersey.server;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.Suspended;

import javax.inject.Singleton;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests {@link CompletionCallback}.
 *
 * @author Miroslav Fuksa
 */
public class AsyncCallbackServerTest {

    private static class Flags {
        public volatile boolean onResumeCalled;
        public volatile boolean onCompletionCalled;
        public volatile boolean onCompletionCalledWithError;
        public volatile boolean onResumeFailedCalled;
    }

    @Test
    public void testCompletionCallback() throws ExecutionException, InterruptedException {
        final Flags flags = new Flags();

        ApplicationHandler app = new ApplicationHandler(
                new ResourceConfig().register(new CompletionResource(flags))
                .register(new CheckingCompletionFilter(flags)));
        ContainerRequest req = RequestContextBuilder.from(
                "/completion/onCompletion", "GET").build();

        final ContainerResponse response = app.apply(req).get();
        assertEquals(200, response.getStatus());
        assertTrue("onComplete() was not called.", flags.onCompletionCalled);
    }

    @Test
    public void testCompletionFail() throws ExecutionException, InterruptedException {
        final Flags flags = new Flags();

        ApplicationHandler app = new ApplicationHandler(
                new ResourceConfig().register(new CompletionResource(flags))
                        .register(new CheckingCompletionFilter(flags)));

        try {
            final ContainerResponse response = app.apply(RequestContextBuilder.from(
                    "/completion/onError", "GET").build()).get();
            fail("should fail");
        } catch (Exception e) {
            // ok - should throw an exception
        }
        assertTrue("onError().", flags.onCompletionCalledWithError);
    }

    @Test
    public void testRegisterNullClass() throws ExecutionException, InterruptedException {
        final ApplicationHandler app = new ApplicationHandler(new ResourceConfig(NullCallbackResource.class));
        final ContainerRequest req = RequestContextBuilder.from("/null-callback/class", "GET").build();

        final ContainerResponse response = app.apply(req).get();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testRegisterNullObject() throws ExecutionException, InterruptedException {
        final ApplicationHandler app = new ApplicationHandler(new ResourceConfig(NullCallbackResource.class));
        final ContainerRequest req = RequestContextBuilder.from("/null-callback/object", "GET").build();

        final ContainerResponse response = app.apply(req).get();
        assertEquals(200, response.getStatus());
    }

    @CompletionBinding
    public static class CheckingCompletionFilter implements ContainerResponseFilter {

        private final Flags flags;

        public CheckingCompletionFilter(Flags flags) {
            this.flags = flags;
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            assertFalse("onComplete() callback has already been called.",
                    flags.onCompletionCalled);
        }
    }

    public static class MyCompletionCallback implements CompletionCallback {

        private final Flags flags;

        public MyCompletionCallback(Flags flags) {
            this.flags = flags;
        }

        @Override
        public void onComplete(Throwable throwable) {
            assertFalse("onComplete() has already been called.", flags.onCompletionCalled);
            assertFalse("onComplete() has already been called with error.", flags.onCompletionCalledWithError);
            if (throwable == null) {
                flags.onCompletionCalled = true;
            } else {
                flags.onCompletionCalledWithError = true;
            }
        }
    }

    @Path("completion")
    public static class CompletionResource {

        private final Flags flags;

        public CompletionResource(Flags flags) {
            this.flags = flags;
        }

        @GET
        @Path("onCompletion")
        @CompletionBinding
        public void onComplete(@Suspended AsyncResponse asyncResponse) {
            assertFalse(flags.onCompletionCalled);
            asyncResponse.register(new MyCompletionCallback(flags));
            asyncResponse.resume("ok");
            assertTrue(flags.onCompletionCalled);
        }

        @GET
        @Path("onError")
        @CompletionBinding
        public void onError(@Suspended AsyncResponse asyncResponse) {
            assertFalse(flags.onCompletionCalledWithError);
            asyncResponse.register(new MyCompletionCallback(flags));
            asyncResponse.resume(new RuntimeException("test-exception"));
            assertTrue(flags.onCompletionCalledWithError);
        }
    }

    @Path("null-callback")
    @Singleton
    public static class NullCallbackResource {

        @GET
        @Path("class")
        @CompletionBinding
        public void registerClass(@Suspended AsyncResponse asyncResponse) {
            try {
                asyncResponse.register(null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(null, MyCompletionCallback.class);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(MyCompletionCallback.class, null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(MyCompletionCallback.class, MyCompletionCallback.class, null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            asyncResponse.resume("ok");
        }

        @GET
        @Path("object")
        @CompletionBinding
        public void registerObject(@Suspended AsyncResponse asyncResponse) {
            try {
                asyncResponse.register((Object) null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(null, new MyCompletionCallback(new Flags()));
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(new MyCompletionCallback(new Flags()), null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            try {
                asyncResponse.register(new MyCompletionCallback(new Flags()), new MyCompletionCallback(new Flags()), null);
                fail("NullPointerException expected.");
            } catch (NullPointerException npe) {
                // Expected.
            }

            asyncResponse.resume("ok");
        }
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface CompletionBinding {
    }
}
