/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.integration.servlet_40_mvc_1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.mvc.Viewable;

@Path("/{client}")
public class ExampleResource {

    @GET
    @Path("html")
    @Produces({ "text/html" })
    public Viewable displayMain(@PathParam("client") String inClientId) {
        return new Viewable("/main");
    }

    @GET
    @Path("string")
    @Produces({ "text/html" })
    public String string(@PathParam("client") String inClientId) {
        return "string string string string string string";
    }
}
