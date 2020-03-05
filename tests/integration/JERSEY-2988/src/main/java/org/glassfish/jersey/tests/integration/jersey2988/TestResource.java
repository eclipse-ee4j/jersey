/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2988;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import javax.inject.Inject;

@Path("/test")
public class TestResource {

    @Inject
    private ContextAwareBean contextAwareBean;

    @GET
    @Path("field")
    public Response field() {
        return contextAwareBean.fieldConfig();
    }

    @GET
    @Path("setter")
    public Response setter() {
        return contextAwareBean.setterConfig();
    }

    @GET
    @Path("ex/field")
    public String fieldExceptionMapper() {
        throw new IllegalStateException("My handled exception.");
    }

    @GET
    @Path("ex/setter")
    public String setterExceptionMapper() {
        throw new NullPointerException("My handled exception.");
    }

}
