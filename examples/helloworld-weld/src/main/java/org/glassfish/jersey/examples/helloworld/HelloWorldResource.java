/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import jakarta.enterprise.context.RequestScoped;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

/**
 * Hello-world JAX-RS resource implemented as CDI bean.
 *
 * @author Jakub Podlesak
 */
@Path("helloworld")
@RequestScoped
public class HelloWorldResource {

    @QueryParam("name")
    String name;

    @GET
    @Produces("text/plain")
    public String getHello() {
        return String.format("Hello %s", name);
    }
}
