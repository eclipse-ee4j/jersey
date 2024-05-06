/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server.resources;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.micrometer.server.exception.ResourceGoneException;

/**
 * @author Michael Weirauch
 */
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class TestResource {

    @Produces(MediaType.TEXT_PLAIN)
    public static class SubResource {

        @GET
        @Path("sub-hello/{name}")
        public String hello(@PathParam("name") String name) {
            return "hello " + name;
        }

    }

    @GET
    public String index() {
        return "index";
    }

    @GET
    @Path("hello")
    public String hello() {
        return "hello";
    }

    @GET
    @Path("hello/{name}")
    public String hello(@PathParam("name") String name) {
        return "hello " + name;
    }

    @GET
    @Path("throws-not-found-exception")
    public String throwsNotFoundException() {
        throw new NotFoundException();
    }

    @GET
    @Path("throws-exception")
    public String throwsException() {
        throw new IllegalArgumentException();
    }

    @GET
    @Path("throws-webapplication-exception")
    public String throwsWebApplicationException() {
        throw new NotAuthorizedException("notauth", Response.status(Status.UNAUTHORIZED).build());
    }

    @GET
    @Path("throws-mappable-exception")
    public String throwsMappableException() {
        throw new ResourceGoneException("Resource has been permanently removed.");
    }

    @GET
    @Path("produces-text-plain")
    @Produces(MediaType.TEXT_PLAIN)
    public String producesTextPlain() {
        return "hello";
    }

    @GET
    @Path("redirect/{status}")
    public Response redirect(@PathParam("status") int status) {
        if (status == 307) {
            throw new RedirectionException(status, URI.create("hello"));
        }
        return Response.status(status).header("Location", "/hello").build();
    }

    @Path("/sub-resource")
    public SubResource subResource() {
        return new SubResource();
    }

}
