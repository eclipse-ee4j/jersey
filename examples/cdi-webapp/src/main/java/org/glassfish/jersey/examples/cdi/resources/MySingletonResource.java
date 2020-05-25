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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;

import jakarta.annotation.ManagedBean;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Shows injection of context objects into the fields of a managed bean.
 *
 * @author Roberto Chinnici
 * @author Jakub Podlesak
 */
@ManagedBean
@ApplicationScoped
@Path("/singleton")
public class MySingletonResource {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Resource(name = "injectedResource")
    int counter = 0;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return String.format("OK %s uri=%s",
                request.getMethod(), uriInfo.getRequestUri());
    }

    @Path("counter")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public synchronized String getCount() {
        return String.format("%d", counter++);
    }

    @Path("counter")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public synchronized void setCount(String c) {
        counter = Integer.decode(c);
    }
}
