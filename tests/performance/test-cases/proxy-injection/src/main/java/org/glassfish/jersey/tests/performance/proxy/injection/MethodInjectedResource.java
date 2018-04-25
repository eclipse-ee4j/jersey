/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Test resource to compare resource method with and without injected method proxiable parameters.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
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
