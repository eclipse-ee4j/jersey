/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.bv;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import javax.enterprise.context.RequestScoped;
import javax.validation.constraints.NotNull;

/**
 * This CDI backed resource should get validated.
 *
 * @author Jakub Podlesak
 */
@Path("old/fashioned")
@RequestScoped
public class CdiOldFashionedResource {

    /**
     * Query param getter.
     *
     * @return query param value.
     */
    @Path("validate")
    @GET
    public String getQ(@QueryParam("q") @NotNull String q) {

        return q;
    }
}
