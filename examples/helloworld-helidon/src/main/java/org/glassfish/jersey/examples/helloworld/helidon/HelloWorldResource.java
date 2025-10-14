/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.helidon;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@Path("helloworld")
public class HelloWorldResource {
    public static final String CLICHED_MESSAGE = "Hello World!";

    @GET
    @Produces("text/plain")
    public String getHello() {
        return CLICHED_MESSAGE;
    }

    @GET
    @Path("query1")
    @Produces("text/plain")
    public String getQueryParameter(@DefaultValue("error1") @QueryParam(value = "test1") String test1,
            @DefaultValue("error2") @QueryParam(value = "test2") String test2) {
        return test1 + test2;
    }

    @POST
    @Path("query2")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postQueryParameter(@DefaultValue("error1") @QueryParam(value = "test1") String test1,
            @DefaultValue("error2") @QueryParam(value = "test2") String test2, String entity) {
        return entity + test1 + test2;
    }

}
