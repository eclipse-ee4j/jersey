package org.glassfish.jersey.apache.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpMethodWithEntityTest extends JerseyTest {

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
            WebTarget r = getWebTarget();
            response = r.request().method("PUT");
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
            WebTarget r = getWebTarget();
            response = r.request().build("DELETE", Entity.text("DELETE")).invoke();
            assertNotEquals(400, response.getStatus());
            assertTrue(response.hasEntity());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testGet() {
        try {
            WebTarget r = getWebTarget();
            response = r.request().build("GET", Entity.text("GET")).invoke();
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
            WebTarget r = getWebTarget();
            response = r.request().build("OPTIONS").invoke();
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void testHead() {
        try {
            WebTarget r = getWebTarget();
            response = r.request().build("HEAD", Entity.text("GET")).invoke();
            assertEquals(200, response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpMethodResource.class);
    }

    protected Client createClient() {
        ClientConfig cc = new ClientConfig();
//        cc.connectorProvider(new ApacheConnectorProvider());
        return ClientBuilder.newClient(cc);
    }

    private WebTarget getWebTarget(final Client client) {
        return client.target(getBaseUri()).path("test");
    }

    private WebTarget getWebTarget() {
        return getWebTarget(createClient());
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
}
