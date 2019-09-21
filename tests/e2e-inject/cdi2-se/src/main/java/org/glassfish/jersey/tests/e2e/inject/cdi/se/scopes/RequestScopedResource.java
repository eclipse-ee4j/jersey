/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.scopes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.inject.Inject;

import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Request scoped resource.
 *
 * @author Petr Bouda
 */
@RequestScoped
@Path("request")
public class RequestScopedResource {

    @Inject
    private ApplicationCounterBean application;

    @PathParam("name")
    private String name;

    private UriInfo uriInfo;

    public RequestScopedResource(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @GET
    @Path("{name}")
    @Produces("text/plain")
    public String getHello() {
        return "Hello_" + name + " [" + application.getNumber() + "] " + "[" + uriInfo.getPath() + "] " + this;
    }
}
