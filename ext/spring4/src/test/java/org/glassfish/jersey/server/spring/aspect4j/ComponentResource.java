/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.aspect4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.spring.TestComponent1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/")
@Component
public class ComponentResource {

    @Autowired
    private TestComponent1 testComponent1;
    @Context
    private UriInfo uriInfo;

    @Path("test2")
    @GET
    public String test2() {
        return testComponent1.result();
    }

    @Path("JERSEY-3126")
    @GET
    public String JERSEY_3126() {
        return uriInfo == null ? "test failed" : "test ok";
    }

}
