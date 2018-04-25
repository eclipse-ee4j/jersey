/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.java8.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * JAX-RS resource inheriting some resource method implementations from the implemented interface.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("default-method")
@Produces("text/plain")
public class DefaultMethodResource implements DefaultMethodInterface {

    @GET
    @Path("class")
    public String fromClass() {
        return "class";
    }
}
