package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpMethodOptionalEntityTest extends JerseyTest {

    private static final String PATH = "test";

    private Response response;

    @After
    public void clear() {
        if (response != null) {
            response.close();
        }
    }

    @Test
    public void testPut() {
        try {
            response = target(PATH).request().method("PUT");

            assertTrue(Arrays.asList(200, 201, 204).contains(response.getStatus()));
            assertEquals("PUT", response.readEntity(String.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            response = target(PATH).request().build("DELETE", Entity.text("DELETE")).invoke();
            assertNotEquals(200, response.getStatus());
            assertTrue(response.hasEntity());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testGet() {
        try {
            response = target(PATH).request().build("GET", Entity.text("GET")).invoke();
            assertNotEquals(400, response.getStatus());
            assertTrue(response.hasEntity());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testOptions() {
        try {
            response = target(PATH).request().build("OPTIONS", Entity.text("OPTIONS")).invoke();
            assertTrue(response.hasEntity());
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testHead() {
        try {
            response = target(PATH).request().build("HEAD", Entity.text("GET")).invoke();
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get(String entity) {
            return entity;
        }

        @DELETE
        public String delete(String entity) {
            return entity;
        }

        @PUT
        public String put() {
            return "PUT";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpMethodResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider());
    }
}
