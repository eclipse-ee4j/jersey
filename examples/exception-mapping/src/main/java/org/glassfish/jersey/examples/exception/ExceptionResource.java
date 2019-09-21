/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.exception;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.examples.exception.Exceptions.MyException;
import org.glassfish.jersey.examples.exception.Exceptions.MySubException;
import org.glassfish.jersey.examples.exception.Exceptions.MySubSubException;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * ExceptionResource class.
 *
 * @author Santiago.PericasGeertsen at oracle.com
 */
@Path("exception")
@Consumes("text/plain")
@Produces("text/plain")
public class ExceptionResource {

    @Provider
    static class MyResponseFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            System.out.println("MyResponseFilter.postFilter() enter");
            responseContext.setEntity(
                    responseContext.getEntity() + ":" + getClass().getSimpleName(), null, MediaType.TEXT_PLAIN_TYPE);
            System.out.println("MyResponseFilter.postFilter() exit");
        }
    }

    @Provider
    static class WebApplicationExceptionFilter implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext context) throws IOException {
            System.out.println("WebApplicationExceptionFilter.preFilter() enter");

            String path = ((ContainerRequest) context).getRequestUri().getPath();
            if (path.endsWith("request_exception") && context.hasEntity() && ((ContainerRequest) context)
                    .readEntity(String.class).equals("Request Exception")) {
                throw new WebApplicationException(Response.Status.OK);
            }
            System.out.println("WebApplicationExceptionFilter.preFilter() exit");
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            System.out.println("WebApplicationExceptionFilter.postFilter() enter");
            if (responseContext.hasEntity() && responseContext.getEntity().equals("Response Exception")) {
                throw new WebApplicationException(Response.Status.OK);
            }
            System.out.println("WebApplicationExceptionFilter.postFilter() exit");
        }
    }

    @GET
    public String pingMe() {
        return "ping!";
    }

    @POST
    @Path("webapplication_entity")
    public String testWebApplicationExceptionEntity(String s) {
        String[] tokens = s.split(":");
        assert tokens.length == 2;
        int statusCode = Integer.valueOf(tokens[1]);
        Response r = Response.status(statusCode).entity(s).build();
        throw new WebApplicationException(r);
    }

    @POST
    @Path("webapplication_noentity")
    public String testWebApplicationExceptionNoEntity(String s) {
        String[] tokens = s.split(":");
        assert tokens.length == 2;
        int statusCode = Integer.valueOf(tokens[1]);
        Response r = Response.status(statusCode).build();
        throw new WebApplicationException(r);
    }

    @POST
    @Path("my")
    public String testMyException(String s) {
        String[] tokens = s.split(":");
        assert tokens.length == 2;
        int statusCode = Integer.valueOf(tokens[1]);
        Response r = Response.status(statusCode).build();
        throw new MyException(r);
    }

    @POST
    @Path("mysub")
    public String testMySubException(String s) {
        String[] tokens = s.split(":");
        assert tokens.length == 2;
        int statusCode = Integer.valueOf(tokens[1]);
        Response r = Response.status(statusCode).build();
        throw new MySubException(r);
    }

    @POST
    @Path("mysubsub")
    public String testMySubSubException(String s) {
        String[] tokens = s.split(":");
        assert tokens.length == 2;
        int statusCode = Integer.valueOf(tokens[1]);
        Response r = Response.status(statusCode).build();
        throw new MySubSubException(r);
    }

    @POST
    @Path("request_exception")
    public String exceptionInRequestFilter() {
        throw new InternalServerErrorException();        // should not reach here
    }

    @GET
    @Path("response_exception")
    public String exceptionInResponseFilter() {
        return "Response Exception";
    }
}
