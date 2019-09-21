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

package org.glassfish.jersey.tests.integration.securitydigest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.SecurityContext;

import javax.inject.Inject;

/**
 * This resource contains methods that are secured using web.xml declarative security. Names of methods
 * describe which user roles have access to them.
 *
 * @author Miroslav Fuksa
 */
@Path("resource")
public class MyResource {
    @Inject
    SecurityContext securityContext;

    @GET
    public String getUserAdmin() {
        return securityContext.getUserPrincipal().getName() + getAuth();
    }

    @POST
    public String postUserAdmin(String entity) {
        return "post-" + entity + "-" + securityContext.getUserPrincipal().getName() + getAuth();
    }

    @GET
    @Path("sub")
    public String getAdmin() {
        return "subget-" + securityContext.getUserPrincipal().getName() + getAuth();
    }

    @Path("locator")
    public Class<SubResource> getSubResourceUserAdmin() {
        return SubResource.class;
    }

    private String getAuth() {
        return "/scheme:" + securityContext.getAuthenticationScheme();
    }

}
