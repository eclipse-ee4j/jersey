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
package org.glassfish.jersey.tests.cdi.client;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@RequestScoped
@Path("/resource")
public class CdiEnabledClientOnServerResource {
    @Inject
    BeanManager beanManager;

    @Context
    UriInfo uriInfo;

    @Path("main")
    @GET
    public Response getResponse() {
        try (Response r = ClientBuilder.newBuilder()
                .register(CdiClientFilter.class, Priorities.USER - 500)
                .register(CdiLowerPriorityClientFilter.class, Priorities.USER)
                .build().target(uriInfo.getBaseUri()).path("/resource/nomain").request().get()) {
            return Response.status(r.getStatus()).build();
        }
    }
}
