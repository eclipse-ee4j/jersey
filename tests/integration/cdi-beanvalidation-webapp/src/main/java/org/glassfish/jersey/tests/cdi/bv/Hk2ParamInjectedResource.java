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

package org.glassfish.jersey.tests.cdi.bv;

import javax.enterprise.inject.Vetoed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * This HK2 managed resource should get validated and validation
 * result injected via resource method parameter.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("validated/param")
@Vetoed
public class Hk2ParamInjectedResource {

    /**
     * Return number of validation issues.
     *
     * @return value from field produced bean.
     */
    @Path("validate")
    @GET
    public int getValidate(@QueryParam("q") @NotNull String q, @Context Hk2ValidationResult validationResult) {

        return validationResult.getViolationCount();
    }
}
