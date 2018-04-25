/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.servlet3.webapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Test resource for the servlet3-webapp example.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("cat")
public class CatResource {
    @GET
    @Produces("text/plain")
    public String meow() {
        return "Miaow!";
    }
}
