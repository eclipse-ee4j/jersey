/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnector;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * An abstract base class for tests of the {@link JavaNetHttpConnector} providing common resources and utility methods.
 */
abstract class AbstractJavaConnectorTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(AbstractJavaConnectorTest.class.getName());
    public static final String RESOURCE_PATH = "java-connector";

    @Path(RESOURCE_PATH)
    public static class JavaConnectorTestResource {
        @GET
        public String helloWorld() {
            return "Hello World!";
        }

        @GET
        @Path("redirect")
        public Response redirectToHelloWorld() throws URISyntaxException {
            return Response.seeOther(new URI(RESOURCE_PATH)).build();
        }

        @POST
        @Path("echo")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.TEXT_PLAIN)
        public String echo(String entity) {
            return entity;
        }

        @POST
        @Path("echo-byte-array")
        @Produces(MediaType.APPLICATION_OCTET_STREAM)
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        public byte[] echoByteArray(byte[] byteArray) {
            return byteArray;
        }

        @POST
        @Path("async")
        public void asyncPostWithTimeout(@QueryParam("timeout") @DefaultValue("10") Long timeoutSeconds,
                                         @Suspended final AsyncResponse asyncResponse,
                                         String message) {
            asyncResponse.setTimeoutHandler(asyncResponse1 ->
                    asyncResponse1.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Timeout").build()));
            asyncResponse.setTimeout(timeoutSeconds, TimeUnit.SECONDS);
            CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    })
                    .handleAsync((unused, throwable) -> throwable != null ? "INTERRUPTED" : message)
                    .thenApplyAsync(asyncResponse::resume);
        }
    }

    protected Response request(String path) {
        return target().path(path).request().get();
    }

    protected Response requestWithEntity(String path, String method, Entity<?> entity) {
        return target().path(path).request().method(method, entity);
    }

    protected Future<Response> requestAsync(String path) {
        return target().path(path).request().async().get();
    }

    protected Future<Response> requestAsyncWithEntity(String path, String method, Entity<?> entity) {
        return target().path(path).request().async().method(method, entity);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(JavaConnectorTestResource.class)
                .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        config.connectorProvider(new JavaNetHttpConnectorProvider());
    }
}
