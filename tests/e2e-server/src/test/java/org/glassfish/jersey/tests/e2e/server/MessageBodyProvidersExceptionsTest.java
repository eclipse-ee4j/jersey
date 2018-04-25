/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * JERSEY-2500 reproducer test.
 *
 * Tests, that correct exceptions are thrown in case no MessageBodyProvider was matched on server.
 *
 * - InternalServerErrorException for MBW (JSR339, chapter 4.2.2, step 7)
 * - NotSupportedException for MBR (JSR339, chapter 4.2.1, step 6)
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class MessageBodyProvidersExceptionsTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(MessageBodyProvidersExceptionsTest.class.getName());

    @Override
    protected Application configure() {
        return new ResourceConfig(
                Resource.class,
                WebAppExceptionMapper.class
        );
    }

    @Path("resource")
    public static class Resource {

        @GET
        @Path("write")
        @Produces(MediaType.TEXT_PLAIN)
        public Resource failOnWrite() {
            return this;
        }

        @POST
        @Path("read")
        @Consumes("foo/bar")
        @Produces(MediaType.TEXT_PLAIN)
        public String failOnRead() {
            return "this-should-never-be-returned";
        }
    }

    @Provider
    public static class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {

        @Override
        public Response toResponse(WebApplicationException exception) {
            LOGGER.fine("ExceptionMapper was invoked.");
            // return the exception class name as an entity for further comparison
            return Response.status(200).header("writer-exception", "after-first-byte").entity(exception.getClass().getName())
                    .build();
        }
    }

    @Test
    public void testReaderThrowsCorrectException() {
        Response response = target().path("resource/write").request(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatus());
        String resString = response.readEntity(String.class);
        // no MBW should have been found, InternalServerErrorException expected
        assertEquals("javax.ws.rs.InternalServerErrorException", resString);
    }

    @Test
    public void testWriterThrowsCorrectException() {
        Response response = target().path("resource/read").request().post(Entity.entity("Hello, world", "text/plain"));
        assertEquals(200, response.getStatus());
        String resString = response.readEntity(String.class);
        // no MBR should have been found, NotSupportedException expected
        assertEquals("javax.ws.rs.NotSupportedException", resString);
    }
}
