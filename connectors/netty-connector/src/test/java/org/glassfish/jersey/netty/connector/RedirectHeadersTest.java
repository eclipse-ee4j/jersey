/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class RedirectHeadersTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(RedirectHeadersTest.class.getName());
    private static final String TEST_URL = "http://localhost:%d/test";
    private static final AtomicReference<String> TEST_URL_REF = new AtomicReference<>();
    private static final String ENTITY = "entity";

    @BeforeEach
    public void before() {
        final String url = String.format(TEST_URL, getPort());
        TEST_URL_REF.set(url);
    }

    @Path("/test")
    public static class RedirectResource {
        @GET
        public String get(@QueryParam("value") String value) {
            return "GET" + value;
        }

        @POST
        public String echo(@QueryParam("value") String value, String entity) {
            return entity + value;
        }

        @GET
        @Path("headers2")
        public String headers(@Context HttpHeaders headers) {
            String encoding = headers.getHeaderString(HttpHeaders.CONTENT_ENCODING);
            String auth = headers.getHeaderString(HttpHeaderNames.PROXY_AUTHORIZATION.toString());
            return encoding + ":" + auth;
        }

        @POST
        @Path("301")
        public Response redirect301(String entity) {
            return Response.status(Response.Status.MOVED_PERMANENTLY)
                    .header(HttpHeaders.LOCATION, URI.create(TEST_URL_REF.get() + "?value=301"))
                    .build();
        }

        @POST
        @Path("302")
        public Response redirect302(String entity) {
            return Response.status(Response.Status.FOUND)
                    .header(HttpHeaders.LOCATION, URI.create(TEST_URL_REF.get() + "?value=302"))
                    .build();
        }

        @POST
        @Path("307")
        public Response redirect307(String entity) {
            return Response.status(Response.Status.TEMPORARY_REDIRECT)
                    .header(HttpHeaders.LOCATION, URI.create(TEST_URL_REF.get()  + "?value=307"))
                    .build();
        }

        @POST
        @Path("308")
        public Response redirectHeaders(String entity) {
            return Response.status(308)
                    .header(HttpHeaders.LOCATION, URI.create(TEST_URL_REF.get() + "?value=308"))
                    .build();
        }


        @POST
        @Path("headers1")
        public Response redirect308(String whatever) {
            return Response.status(301).header(HttpHeaders.LOCATION, URI.create(TEST_URL_REF.get() + "/headers2")).build();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(RedirectResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.FOLLOW_REDIRECTS, true);
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    void testPost() {
        testPost("301");
        testPost("302");
        testPost("307");
        testPost("308");
    }

    @Test
    void testGet() {
        Assertions.assertEquals("GET301", testGet("301"));
        Assertions.assertEquals("GET302", testGet("302"));
        Assertions.assertEquals(ENTITY + "307", testGet("307"));
        Assertions.assertEquals(ENTITY + "308", testGet("308"));
    }

    @Test
    void testHeaders() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
        headers.add(HttpHeaderNames.PROXY_AUTHORIZATION.toString(), "basic aGVsbG86d29ybGQ=");
        try (Response response = target("test")
                .property(NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT, false)
                .path("headers1").request().headers(headers).post(Entity.entity(ENTITY, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals("null:null", response.readEntity(String.class));
        }

    }

    void testPost(String status) {
        try (Response response = target("test").path(status).request().post(Entity.entity(ENTITY, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals(ENTITY + status, response.readEntity(String.class));
        }
    }

    String testGet(String status) {
        try (Response response = target("test")
                .property(NettyClientProperties.PRESERVE_METHOD_ON_REDIRECT, false)
                .path(status).request().post(Entity.entity(ENTITY, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            return response.readEntity(String.class);
        }
    }
}
