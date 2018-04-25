/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j441.one;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * CDI backed JAX-RS resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/test")
@RequestScoped
public class CdiResource {

    @Context
    private ServletContext scField;

    private ServletContext scCtorParam;

    /* to make CDI happy */
    public CdiResource() {
    }

    @Inject
    public CdiResource(@Context final ServletContext sc) {
        this.scCtorParam = sc;
    }

    @GET
    @Path("ctor-param")
    public String getCtorParam() {
        return scCtorParam.getContextPath();
    }

    @GET
    @Path("method-param")
    public String getMethodParam(@Context final ServletContext sc) {
        return sc.getContextPath();
    }

    @GET
    @Path("field")
    public String getField() {
        return scField.getContextPath();
    }

    @GET
    @Path("exception")
    public String getException() throws Exception {
        throw new Exception() {};
    }
}
