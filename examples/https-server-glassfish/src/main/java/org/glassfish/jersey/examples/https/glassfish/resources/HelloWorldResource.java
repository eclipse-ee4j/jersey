/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.https.glassfish.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The JAX-RS resource class will be hosted at the URI path {@code "/helloworld"}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
@Path("/helloworld")
@RolesAllowed("myRole")
public class HelloWorldResource {

    /**
     * HTTP GET controller method.
     *
     * @param request HTTP servlet request.
     * @return a simple text message.
     */
    @GET
    @Produces("text/plain")
    public Response getClichedMessage(@Context HttpServletRequest request) {
        return Response
                .status(Response.Status.OK)
                .type("text/plain")
                .entity("Sending \"Hello World\" to user \"" + request.getUserPrincipal().getName() + "\"")
                .build();

    }
}
