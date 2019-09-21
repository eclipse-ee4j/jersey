/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedbeans.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.annotation.ManagedBean;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

/**
 * JAX-RS root resource treated as managed bean.
 *
 * @author Paul Sandoz
 */
@Path("/managedbean/per-request")
@ManagedBean
public class ManagedBeanPerRequestResource {

    @Context UriInfo ui;

    @QueryParam("x") String x;

    public static class MyInterceptor {

        @AroundInvoke
        public Object around(InvocationContext ctx) throws Exception {
            return String.format("INTERCEPTED: %s", ctx.proceed());
        }
    }

    @GET
    @Produces("text/plain")
    @Interceptors(MyInterceptor.class)
    public String getMessage() {
        return String.format("echo from %s: %s", ui.getPath(), x);
    }

    @Path("exception")
    public String getException() {
        throw new ManagedBeanException();
    }
}
