/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import java.security.Principal;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import static java.util.Optional.ofNullable;

/**
 * Singleton-scoped resource.
 *
 * @author Petr Bouda
 */
@Singleton
@Path("helloworld")
public class HelloWorldResource {

    @Inject
    private HelloBean helloBean;

    @GET
    @Path("{name}")
    @Produces("text/plain")
    public String getHello(@PathParam("name") String name, @Context SecurityContext sc) {
        final StringBuilder sb = new StringBuilder(this.helloBean.hello(name));

        ofNullable(sc.getUserPrincipal())
                .map(Principal::getName)
                .ifPresent(p -> {
                    sb.append("(");
                    sb.append(p);
                    sb.append(")");
                });

        return sb.toString();
    }
}
