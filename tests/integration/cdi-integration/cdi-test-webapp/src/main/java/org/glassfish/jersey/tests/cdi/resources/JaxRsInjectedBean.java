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

package org.glassfish.jersey.tests.cdi.resources;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * This is to be JAX-RS injected at runtime.
 * The bean is fully CDI managed, contains JAX-RS injection points and is getting injected into JAX-RS
 * resources included in two distinct JAX-RS applications running in parallel.
 */
@RequestScoped
public class JaxRsInjectedBean {

    @HeaderParam("x-test")
    String testHeader;

    @Context
    UriInfo uriInfo;

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public String getTestHeader() {
        return testHeader;
    }
}
