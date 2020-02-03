/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.proxy.injection;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

/**
 * Test resource to compare resource method with and without injected method proxiable parameters.
 *
 * @author Jakub Podlesak
 */
@Path("method-injected")
@Produces(MediaType.TEXT_PLAIN)
public class MethodInjectedResource {

    @GET
    @Path("without-parameters")
    public String getNoProxy() {
        return "text";
    }

    @GET
    @Path("all-parameters")
    public String getProxy(@Context SecurityContext securityContext,
                           @Context UriInfo uriInfo,
                           @Context HttpHeaders httpHeaders,
                           @Context Request request) {
        return String.format("sc: %s\nui: %s\nhh: %s\nreq: %s",
                securityContext.getClass(), uriInfo.getClass(), httpHeaders.getClass(), request.getClass());
    }
}
