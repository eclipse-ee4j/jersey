/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;

import jakarta.annotation.ManagedBean;

import jakarta.enterprise.context.RequestScoped;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;

/**
 * Shows injection of context objects and path parameters into the fields of a managed bean.
 *
 * @author Roberto Chinnici
 * @author Jakub Podlesak
 */
@ManagedBean
@RequestScoped
@Path("/other/{c}/{d}")
public class MyOtherResource {

    public static class MyInterceptor {

        @AroundInvoke
        public Object around(InvocationContext ctx) throws Exception {
            return String.format("INTERCEPTED: %s", ctx.proceed());
        }
    }

    @Context UriInfo uriInfo;
    @Context Request request;

    @PathParam("c") String c;
    @PathParam("d") String d;

    @GET
    @Produces("text/plain")
    @Interceptors(MyInterceptor.class)
    public String get() {
        return String.format("OK %s %s, c=%s, d=%s", request.getMethod(), uriInfo.getRequestUri(), c, d);
    }
}
