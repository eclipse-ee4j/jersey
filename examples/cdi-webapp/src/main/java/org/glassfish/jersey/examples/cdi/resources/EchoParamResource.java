/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.annotation.ManagedBean;

/**
 * A managed bean that uses (but does not inject) a path parameter.
 *
 * @author Roberto Chinnici
 */
@ManagedBean
@Path("echo/{a}")
public class EchoParamResource {

    @GET
    @Produces("text/plain")
    public String get(@PathParam("a") String param) {
        return "ECHO " + param;
    }
}
