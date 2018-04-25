/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Response E2E tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ResponseE2ETest extends JerseyTest {

    /**
     * Custom OK response.
     */
    public static class OkResponse extends Response {

        private Response r;

        /**
         * Custom OK response constructor.
         *
         * @param entity entity content.
         */
        public OkResponse(String entity) {
            r = Response.ok(entity).build();
        }

        @Override
        public int getStatus() {
            return r.getStatus();
        }

        @Override
        public StatusType getStatusInfo() {
            return r.getStatusInfo();
        }

        @Override
        public Object getEntity() {
            return r.getEntity();
        }

        @Override
        public <T> T readEntity(Class<T> entityType) {
            return r.readEntity(entityType);
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType) {
            return r.readEntity(entityType);
        }

        @Override
        public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
            return r.readEntity(entityType, annotations);
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
            return r.readEntity(entityType, annotations);
        }

        @Override
        public boolean hasEntity() {
            return r.hasEntity();
        }

        @Override
        public boolean bufferEntity() {
            return r.bufferEntity();
        }

        @Override
        public void close() {
            r.close();
        }

        @Override
        public MediaType getMediaType() {
            return r.getMediaType();
        }

        @Override
        public Locale getLanguage() {
            return r.getLanguage();
        }

        @Override
        public int getLength() {
            return r.getLength();
        }

        @Override
        public Set<String> getAllowedMethods() {
            return r.getAllowedMethods();
        }

        @Override
        public Map<String, NewCookie> getCookies() {
            return r.getCookies();
        }

        @Override
        public EntityTag getEntityTag() {
            return r.getEntityTag();
        }

        @Override
        public Date getDate() {
            return r.getDate();
        }

        @Override
        public Date getLastModified() {
            return r.getLastModified();
        }

        @Override
        public URI getLocation() {
            return r.getLocation();
        }

        @Override
        public Set<Link> getLinks() {
            return r.getLinks();
        }

        @Override
        public boolean hasLink(String relation) {
            return r.hasLink(relation);
        }

        @Override
        public Link getLink(String relation) {
            return r.getLink(relation);
        }

        @Override
        public Link.Builder getLinkBuilder(String relation) {
            return r.getLinkBuilder(relation);
        }

        @Override
        public MultivaluedMap<String, Object> getMetadata() {
            return r.getMetadata();
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return r.getHeaders();
        }

        @Override
        public MultivaluedMap<String, String> getStringHeaders() {
            return r.getStringHeaders();
        }

        @Override
        public String getHeaderString(String name) {
            return r.getHeaderString(name);
        }
    }

    @Path("response")
    public static class ResponseTestResource {

        @GET
        @Path("custom")
        public OkResponse subresponse() {
            return new OkResponse("subresponse");
        }

        @GET
        @Path("null")
        public Response nullResponse() {
            return null;
        }

        @GET
        @Path("no-status-with-entity")
        public Response entityResponseTest() {
            return RuntimeDelegate.getInstance().createResponseBuilder().entity("1234567890").build();
        }

        @GET
        @Path("no-status-without-entity")
        public Response noEntityResponseTest() {
            return RuntimeDelegate.getInstance().createResponseBuilder().build();
        }

        @Uri("response/internal")
        WebTarget target;

        @GET
        @Path("external")
        public Response external() {
            Response response;
            if (target == null) {
                response = Response.serverError().entity("injected WebTarget is null").build();
            } else {
                response = target.request().buildGet().invoke();
            }
            return response;
        }

        @GET
        @Path("internal")
        public String internal() {
            return "internal";
        }

        @PUT
        @Path("not-modified-put")
        public Response notModifiedPut(String data) {
            return Response.notModified().entity("not-modified-" + data).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ResponseTestResource.class);
    }

    /**
     * JERSEY-1516 reproducer.
     */
    @Test
    public void testCustomResponse() {
        final Response response = target("response").path("custom").request().get();

        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", 200, response.getStatus());
        assertEquals("Unexpected response entity.", "subresponse", response.readEntity(String.class));
    }

    /**
     * JERSEY-1527 reproducer.
     */
    @Test
    public void testNoStatusResponse() {
        final WebTarget target = target("response").path("no-status-{param}-entity");
        Response response;

        response = target.resolveTemplate("param", "with").request().get();
        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", 200, response.getStatus());
        assertEquals("Unexpected response entity.", "1234567890", response.readEntity(String.class));

        response = target.resolveTemplate("param", "without").request().get();
        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", 204, response.getStatus());
        assertFalse("Unexpected non-empty response entity.", response.hasEntity());
    }

    /**
     * JERSEY-1528 reproducer.
     */
    @Test
    public void testNullResponse() {
        final Response response = target("response").path("null").request().get();

        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", 204, response.getStatus());
        assertFalse("Unexpected non-empty response entity.", response.hasEntity());
    }

    /**
     * JERSEY-1531 reproducer.
     */
    @Test
    public void testInboundOutboundResponseMixing() {
        final WebTarget target = target("response").path("external");
        Response response;

        response = target.request().get();
        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", 200, response.getStatus());
        assertEquals("Unexpected response entity.", "internal", response.readEntity(String.class));
    }

    /**
     * JERSEY-845 reproducer.
     *
     * Verifies consistent behavior over time ("works as designed").
     */
    @Test
    public void testEntityInNotModifiedPutResposne() {
        final WebTarget target = target("response").path("not-modified-put");
        Response response;

        response = target.request().put(Entity.text("put-data"));
        assertNotNull("Response is null.", response);
        assertEquals("Unexpected response status.", Response.Status.NOT_MODIFIED.getStatusCode(), response.getStatus());
        // response entity is dropped by server container in compliance with HTTP 1.1 spec
        assertFalse("Unexpected response entity.", response.hasEntity());
    }
}
