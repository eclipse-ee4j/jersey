/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.netty;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author Jakub Podlesak
 */
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
