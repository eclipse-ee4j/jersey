package org.glassfish.jersey.netty.httpserver;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JerseyServerHandlerTest extends AbstractNettyServerTester {

    private Client client;

    @Before
    public void setUp() throws Exception {
        startServer(Resource.class);
        client = ClientBuilder.newClient();
    }

    @Test
    public void testWithSubPath() {
        Response r = client.target(getUri().path("resource/ping/")).request().get();
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals((Integer) 1, r.readEntity(Integer.class));
    }

    @Test
    public void testWithPath() {
        Response r = client.target(getUri().path("resource")).request().get();
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals((Integer) 2, r.readEntity(Integer.class));
    }

    @Path("resource")
    public static class Resource {

        @GET
        @Path("ping")
        public Response get() {
            return Response.ok(1).build();
        }

        @GET
        public Response get1() {
            return Response.ok(2).build();
        }
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        client = null;
    }
}
