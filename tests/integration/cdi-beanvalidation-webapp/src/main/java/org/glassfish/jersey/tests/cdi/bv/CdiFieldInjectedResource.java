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

import javax.enterprise.context.RequestScoped;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * This CDI backed resource should get validated and validation result field injected.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("validated/field")
@RequestScoped
public class CdiFieldInjectedResource {

    @Inject
    ValidationResult validationResult;

    @Inject
    NonJaxRsValidatedBean cdiBean;

    @QueryParam("q")
    @NotNull
    String q;

    /**
     * Return number of validation issues.
     *
     * @return value from field injected validation bean.
     */
    @Path("validate")
    @GET
    public int getValidate() {

        return validationResult.getViolationCount();
    }

    /**
     * Return number of validation issues for non JAX-RS bean.
     * This is to make sure the implicit Hibernate validator
     * is functioning for raw CDI beans, where Jersey
     * is not involved in method invocation.
     *
     * @return number of validation issues revealed when invoking injected CDI bean.
     */
    @Path("validate/non-jaxrs")
    @GET
    public int getValidateNonJaxRs(@QueryParam("h") String h) {

        try {
            cdiBean.echo(h);
            return 0;
        } catch (ConstraintViolationException ex) {
            return ex.getConstraintViolations().size();
        }
    }

}
