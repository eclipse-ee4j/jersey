/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class HttpMethodTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpMethodResource.class, ErrorResource.class);
    }

    protected Client createClient() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        return ClientBuilder.newClient(cc);
    }

    protected Client createPoolingClient() {
        ClientConfig cc = new ClientConfig();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(100);
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, connectionManager);
        cc.connectorProvider(new Apache5ConnectorProvider());
        return ClientBuilder.newClient(cc);
    }

    private WebTarget getWebTarget(final Client client) {
        return client.target(getBaseUri()).path("test");
    }

    private WebTarget getWebTarget() {
        return getWebTarget(createClient());
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH {
    }

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }

        @POST
        public String post(String entity) {
            return entity;
        }

        @PUT
        public String put(String entity) {
            return entity;
        }

        @DELETE
        public String delete() {
            return "DELETE";
        }

        @DELETE
        @Path("withentity")
        public String delete(String entity) {
            return entity;
        }

        @POST
        @Path("noproduce")
        public void postNoProduce(String entity) {
        }

        @POST
        @Path("noconsumeproduce")
        public void postNoConsumeProduce() {
        }

        @PATCH
        public String patch(String entity) {
            return entity;
        }
    }

    @Test
    public void testHead() {
        WebTarget r = getWebTarget();
        Response cr = r.request().head();
        assertFalse(cr.hasEntity());
    }

    @Test
    public void testOptions() {
        WebTarget r = getWebTarget();
        Response cr = r.request().options();
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testOptionsWithEntity() {
        WebTarget r = getWebTarget();
        Response response = r.request().build("OPTIONS", Entity.text("OPTIONS")).invoke();
        assertEquals(200, response.getStatus());
        response.close();
    }

    @Test
    public void testGet() {
        WebTarget r = getWebTarget();
        assertEquals("GET", r.request().get(String.class));

        Response cr = r.request().get();
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPost() {
        WebTarget r = getWebTarget();
        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));

        Response cr = r.request().post(Entity.text("POST"));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPostChunked() {
        ClientConfig cc = new ClientConfig()
                .property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024)
                .connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = getWebTarget(client);

        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));

        Response cr = r.request().post(Entity.text("POST"));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPostVoid() {
        WebTarget r = getWebTarget(createPoolingClient());

        for (int i = 0; i < 100; i++) {
            r.request().post(Entity.text("POST"));
        }
    }

    @Test
    public void testPostNoProduce() {
        WebTarget r = getWebTarget();
        assertEquals(204, r.path("noproduce").request().post(Entity.text("POST")).getStatus());

        Response cr = r.path("noproduce").request().post(Entity.text("POST"));
        assertFalse(cr.hasEntity());
        cr.close();
    }


    @Test
    public void testPostNoConsumeProduce() {
        WebTarget r = getWebTarget();
        assertEquals(204, r.path("noconsumeproduce").request().post(null).getStatus());

        Response cr = r.path("noconsumeproduce").request().post(Entity.text("POST"));
        assertFalse(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPut() {
        WebTarget r = getWebTarget();
        assertEquals("PUT", r.request().put(Entity.text("PUT"), String.class));

        Response cr = r.request().put(Entity.text("PUT"));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testDelete() {
        WebTarget r = getWebTarget();
        assertEquals("DELETE", r.request().delete(String.class));

        Response cr = r.request().delete();
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPatch() {
        WebTarget r = getWebTarget();
        assertEquals("PATCH", r.request().method("PATCH", Entity.text("PATCH"), String.class));

        Response cr = r.request().method("PATCH", Entity.text("PATCH"));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testAll() {
        WebTarget r = getWebTarget();

        assertEquals("GET", r.request().get(String.class));

        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));

        assertEquals(204, r.path("noproduce").request().post(Entity.text("POST")).getStatus());

        assertEquals(204, r.path("noconsumeproduce").request().post(null).getStatus());

        assertEquals("PUT", r.request().post(Entity.text("PUT"), String.class));

        assertEquals("DELETE", r.request().delete(String.class));
    }


    @Path("/error")
    public static class ErrorResource {
        @POST
        public Response post(String entity) {
            return Response.serverError().build();
        }

        @Path("entity")
        @POST
        public Response postWithEntity(String entity) {
            return Response.serverError().entity("error").build();
        }
    }

    @Test
    public void testPostError() {
        WebTarget r = createClient().target(getBaseUri()).path("error");

        for (int i = 0; i < 100; i++) {
            try {
                final Response post = r.request().post(Entity.text("POST"));
                post.close();
            } catch (ClientErrorException ex) {
            }
        }
    }

    @Test
    public void testPostErrorWithEntity() {
        WebTarget r = createPoolingClient().target(getBaseUri()).path("error/entity");

        for (int i = 0; i < 100; i++) {
            try {
                r.request().post(Entity.text("POST"));
            } catch (ClientErrorException ex) {
                String s = ex.getResponse().readEntity(String.class);
                assertEquals("error", s);
            }
        }
    }
}
