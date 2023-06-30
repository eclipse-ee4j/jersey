/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.configured.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;


@Path("helloworld")
public class HelloWorldResource {
    @Context
    Configuration configuration;

    @POST
    @Produces("text/plain")
    public String postHello(String helloMsg) {
        return helloMsg;
    }

    @GET
    @Path("agent")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAgent(@Context HttpHeaders headers) {
        return headers.getHeaderString(HttpHeaders.USER_AGENT);
    }

}
