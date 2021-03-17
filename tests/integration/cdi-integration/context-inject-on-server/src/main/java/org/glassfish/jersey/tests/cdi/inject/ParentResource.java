/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

public class ParentResource extends RequestScopedParentInject {

    @Context
    ContainerRequestContext requestContext;

    @GET
    @Path("injected")
    public Response areInjected() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean injected = checkInjected(stringBuilder);

        if (injected) {
            return Response.ok().entity("All injected").build();
        } else {
            requestContext.setProperty(ParentWriterInterceptor.STATUS, Response.Status.EXPECTATION_FAILED);
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(stringBuilder.toString()).build();
        }
    }

    @GET
    @Path("contexted")
    public Response areContexted() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean contexted = checkContexted(stringBuilder);

        if (contexted) {
            return Response.ok().entity("All contexted").build();
        } else {
            requestContext.setProperty(ParentWriterInterceptor.STATUS, Response.Status.EXPECTATION_FAILED);
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(stringBuilder.toString()).build();
        }
    }

    @GET
    @Path("iae/{x}")
    public Response iae(@PathParam("x") String injected) {
        throw new IllegalArgumentException(injected);
    }
}
