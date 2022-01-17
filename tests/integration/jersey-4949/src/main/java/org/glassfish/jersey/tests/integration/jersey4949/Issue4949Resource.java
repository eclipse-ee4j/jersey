/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4949;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Test resource.
 */
@Path("/")
public class Issue4949Resource {
    public static final String PATH = "0.0.2%20-%20Market%20Data%20Import";
    @GET
    @Path(PATH)
    public String get(@Context UriInfo uriInfo) {
        return uriInfo.getRequestUri().toASCIIString();
    }

    @GET
    @Path("echo")
    public String echo(@Context UriInfo uriInfo) {
        return uriInfo.getRequestUri().toASCIIString();
    }
}
