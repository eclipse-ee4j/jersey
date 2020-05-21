/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyCompletionStageRxInvoker;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class BasicHelidonConnectorTest extends JerseyTest {

    private final String entityType;

    public BasicHelidonConnectorTest(String entityType) {
        this.entityType = entityType;
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[]{"BYTE_ARRAY_OUTPUT_STREAM", "READABLE_BYTE_CHANNEL", "OUTPUT_STREAM_PUBLISHER"};
    }

    @Path("basic")
    public static class BasicResource {
        @Path("get")
        @GET
        public String get() {
            return "ok";
        }

        @Path("getquery")
        @GET
        public String getQuery(@QueryParam("first") String first, @QueryParam("second") String second) {
            return first + second;
        }

        @POST
        @Path("post")
        public String post(String entity) {
            return entity + entity;
        }

        @GET
        @Path("headers")
        public Response headers(@Context HttpHeaders headers) {
            Response.ResponseBuilder response = Response.ok("ok");
            for (Map.Entry<String, List<String>> set : headers.getRequestHeaders().entrySet()) {
                if (set.getKey().toUpperCase(Locale.ROOT).startsWith("X-TEST")) {
                    response.header(set.getKey(), set.getValue().iterator().next());
                }
            }
            return response.build();
        }

        @PUT
        @Consumes("test/x-test")
        @Produces("test/y-test")
        @Path("produces/consumes")
        public String putConsumesProduces(String content) {
            return content + content;
        }
    }

    @Path("async")
    public static class AsyncResource {
        private static CountDownLatch shortLong = null;

        @GET
        @Path("reset")
        public void reset() {
            shortLong = new CountDownLatch(1);
        }

        @Path("long")
        @GET
        public String longGet() throws InterruptedException {
            shortLong.await(10000, TimeUnit.MILLISECONDS);
            return shortLong.getCount() == 0 ? "long" : "shortLong CountDownLatch has not been hit";
        }

        @Path("short")
        @GET
        public String shortGet() {
            shortLong.countDown();
            return "short";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(BasicResource.class, AsyncResource.class)
                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");
    }

    @Override
    protected void configureClient(ClientConfig config) {
        super.configureClient(config);
        config.connectorProvider(new HelidonConnectorProvider());
        config.property("jersey.config.helidon.client.entity.type", entityType);
    }

    @Test
    public void testBasicGet() {
        try (Response response = target("basic").path("get").request().get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("ok", response.readEntity(String.class));
        }
    }

    @Test
    public void testBasicPost() {
        try (Response response = target("basic").path("post").request()
                .buildPost(Entity.entity("ok", MediaType.TEXT_PLAIN_TYPE)).invoke()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("okok", response.readEntity(String.class));
        }
    }

    @Test
    public void queryGetTest() {
        try (Response response = target("basic").path("getquery")
                .queryParam("first", "hello")
                .queryParam("second", "world")
                .request().get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("helloworld", response.readEntity(String.class));
        }
    }

    @Test
    public void testHeaders() {
        String[][] headers = new String[][]{{"X-TEST-ONE", "ONE"}, {"X-TEST-TWO", "TWO"}, {"X-TEST-THREE", "THREE"}};
        MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        Arrays.stream(headers).forEach(a -> map.add(a[0], a[1]));
        try (Response response = target("basic").path("headers").request().headers(map).get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("ok", response.readEntity(String.class));
            for (int i = 0; i != headers.length; i++) {
                Assert.assertTrue(response.getHeaders().containsKey(headers[i][0]));
                Assert.assertEquals(headers[i][1], response.getStringHeaders().getFirst(headers[i][0]));
            }
        }
    }

    @Test
    public void testProduces() {
        try (Response response = target("basic").path("produces/consumes").request("test/z-test")
                .put(Entity.entity("ok", new MediaType("test", "x-test")))) {
            Assert.assertEquals(406, response.getStatus());
        }

        try (Response response = target("basic").path("produces/consumes").request()
                .put(Entity.entity("ok", new MediaType("test", "x-test")))) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("okok", response.readEntity(String.class));
            Assert.assertEquals("test/y-test", response.getStringHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        }
    }

    @Test
    public void testAsyncGet() throws ExecutionException, InterruptedException {
        Future<Response> futureResponse = target("basic").path("get").request().async().get();
        try (Response response = futureResponse.get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("ok", response.readEntity(String.class));
        }
    }

    @Test
    public void testConsumes() {
        try (Response response = target("basic").path("produces/consumes").request("test/y-test")
                .put(Entity.entity("ok", new MediaType("test", "z-test")))) {
            Assert.assertEquals(415, response.getStatus());
        }

        try (Response response = target("basic").path("produces/consumes").request("test/y-test")
                .put(Entity.entity("ok", MediaType.WILDCARD_TYPE))) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("okok", response.readEntity(String.class));
            Assert.assertEquals("test/y-test", response.getStringHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        }
    }

    @Test
    public void testRxGet() throws ExecutionException, InterruptedException {
        CompletableFuture<Response> futureResponse =
                target("basic").path("get").request().rx(JerseyCompletionStageRxInvoker.class).get();

        try (Response response = futureResponse.get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("ok", response.readEntity(String.class));
        }
    }

    @Test
    public void testInputStreamEntity() throws IOException {
        try (Response response = target("basic").path("get").request().get()) {
            Assert.assertEquals(200, response.getStatus());
            InputStream is = response.readEntity(InputStream.class);
            Assert.assertEquals('o', is.read());
            Assert.assertEquals('k', is.read());
            is.close();
        }
    }

    // -----------Async

    @Test
    public void testTwoClientsAsync() throws ExecutionException, InterruptedException {
        try (Response resetResponse = target("async").path("reset").request().get()) {
            Assert.assertEquals(204, resetResponse.getStatus());
        }

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new HelidonConnectorProvider());

        Client longClient = ClientBuilder.newClient(config);
        Invocation.Builder longRequest = longClient.target(getBaseUri()).path("async/long").request();

        Client shortClient = ClientBuilder.newClient(config);
        Invocation.Builder shortRequest = shortClient.target(getBaseUri()).path("async/short").request();

        Future<Response> futureLongResponse = longRequest.async().get();
        Future<Response> futureShortResponse = shortRequest.async().get();

        try (Response shortResponse = futureShortResponse.get()) {
            Assert.assertEquals(200, shortResponse.getStatus());
            Assert.assertEquals("short", shortResponse.readEntity(String.class));
        }

        try (Response longResponse = futureLongResponse.get()) {
            Assert.assertEquals(200, longResponse.getStatus());
            Assert.assertEquals("long", longResponse.readEntity(String.class));
        }
    }

    @Test
    public void testOneClientsTwoReqestsAsync() throws ExecutionException, InterruptedException {
        try (Response resetResponse = target("async").path("reset").request().get()) {
            Assert.assertEquals(204, resetResponse.getStatus());
        }

        Invocation.Builder longRequest = target().path("async/long").request();
        Invocation.Builder shortRequest = target().path("async/short").request();

        Future<Response> futureLongResponse = longRequest.async().get();
        Future<Response> futureShortResponse = shortRequest.async().get();

        try (Response shortResponse = futureShortResponse.get()) {
            Assert.assertEquals(200, shortResponse.getStatus());
            Assert.assertEquals("short", shortResponse.readEntity(String.class));
        }

        try (Response longResponse = futureLongResponse.get()) {
            Assert.assertEquals(200, longResponse.getStatus());
            Assert.assertEquals("long", longResponse.readEntity(String.class));
        }
    }
}
