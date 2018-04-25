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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests HeaderValueException.
 *
 * @author Miroslav Fuksa
 */
public class HeaderValueExceptionTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ResponseFilter.class, TestResource.class);
    }

    @Test
    public void testInboundHeaderThrowsException() throws ExecutionException, InterruptedException {
        final Response response = target("resource/inbound").request()
                .header(HttpHeaders.DATE, "foo")
                .post(Entity.entity("inbound", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(400, response.getStatus());
    }

    @Test
    public void testOutboundHeaderThrowsException() throws ExecutionException, InterruptedException {
        final Response response = target("resource/outbound").request()
                .post(Entity.entity("outbound", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(500, response.getStatus());
    }

    @Test
    public void testOutboundResponseHeaderThrowsException() throws ExecutionException, InterruptedException {
        final Response response = target("resource/outbound-Response").request()
                .post(Entity.entity("outbound", MediaType.TEXT_PLAIN_TYPE));
        Assert.assertEquals(500, response.getStatus());
    }


    public static class ResponseFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            // this call should throw HeaderValueException which will be converted to HTTP 500 response
            responseContext.getDate();
        }
    }

    @Path("resource")
    public static class TestResource {
        @POST
        @Path("inbound")
        public String postInbound(String entity, @Context HttpHeaders headers) {
            // this call should throw HeaderValueException which will be converted to HTTP 400 response
            headers.getDate();
            return entity;
        }

        @POST
        @Path("outbound")
        public Response postOutbound(String entity) {
            return Response.ok().entity(entity).header(HttpHeaders.DATE, "bar").build();
        }

        @POST
        @Path("outbound-Response")
        public Response postOutboundResponse(String entity) {
            final Response response = Response.ok(entity).header(HttpHeaders.DATE, "foo").build();
            // this call should throw HeaderValueException which will be converted to HTTP 500 response
            response.getDate();
            return response;
        }
    }
}
