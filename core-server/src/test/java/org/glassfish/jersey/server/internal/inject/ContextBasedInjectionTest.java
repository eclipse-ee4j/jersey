/*
 * Copyright (c) 2011, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import javax.inject.Provider;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.model.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for creating an application with asynchronously handled request processing
 * via {@link Resource}'s programmatic API.
 *
 * @author Marek Potociar
 */
public class ContextBasedInjectionTest {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");

    public static Stream<Arguments> testUriSuffixes() {
        return Stream.of(
                Arguments.of("a/b/c", "A-B-C"),
                Arguments.of("a/b/d/", "A-B-D")
        );
    }

    private static class AsyncInflector implements Inflector<ContainerRequestContext, Response> {

        @Context
        private Provider<AsyncContext> asyncContextProvider;
        private final String responseContent;

        public AsyncInflector() {
            this.responseContent = "DEFAULT";
        }

        public AsyncInflector(String responseContent) {
            this.responseContent = responseContent;
        }

        @Override
        public Response apply(final ContainerRequestContext req) {
            // Suspend current request
            final AsyncContext asyncContext = asyncContextProvider.get();
            asyncContext.suspend();

            Executors.newSingleThreadExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }

                    // Returning will enter the suspended request
                    asyncContext.resume(Response.ok().entity(responseContent).build());
                }
            });

            return null;
        }
    }

    private ApplicationHandler app;

    @BeforeEach
    public void setupApplication() {
        ResourceConfig rc = new ResourceConfig();

        Resource.Builder rb;
        rb = Resource.builder("a/b/c");
        rb.addMethod("GET").handledBy(new AsyncInflector("A-B-C"));
        rc.registerResources(rb.build());

        rb = Resource.builder("a/b/d");
        rb.addMethod("GET").handledBy(new AsyncInflector("A-B-D"));
        rc.registerResources(rb.build());

        app = new ApplicationHandler(rc);
    }

    @ParameterizedTest
    @MethodSource("testUriSuffixes")
    public void testAsyncApp(String uriSuffix, String expectedResponse) throws InterruptedException, ExecutionException {
        ContainerRequest req =
                RequestContextBuilder.from(BASE_URI, URI.create(BASE_URI.getPath() + uriSuffix), "GET").build();

        Future<ContainerResponse> res = app.apply(req);

        assertEquals(expectedResponse, res.get().getEntity());
    }
}
