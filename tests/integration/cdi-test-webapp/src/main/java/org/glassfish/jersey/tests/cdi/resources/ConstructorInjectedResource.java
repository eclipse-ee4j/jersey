/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * JERSEY-2526 reproducer. CDI managed JAX-RS root resource
 * that is constructor injected with JAX-RS parameters provided by Jersey
 * and a single String parameter coming from application provided CDI producer,
 * {@link CustomCdiProducer}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("ctor-injected/{p}")
@RequestScoped
public class ConstructorInjectedResource {

    String pathParam;
    String queryParam;
    String matrixParam;
    String headerParam;
    String cdiParam;

    /**
     * WLS requires this.
     */
    public ConstructorInjectedResource() {
    }

    /**
     * This will get CDI injected with JAX-RS provided parameters.
     *
     * @param pathParam path parameter from the actual request.
     * @param queryParam query parameter q from the actual request.
     * @param matrixParam matrix parameter m from the actual request.
     * @param headerParam Accept header parameter from the actual request.
     * @param cdiParam custom CDI produced string.
     */
    @Inject
    public ConstructorInjectedResource(
            @PathParam("p") String pathParam,
            @QueryParam("q") String queryParam,
            @MatrixParam("m") String matrixParam,
            @HeaderParam("Custom-Header") String headerParam,
            @CustomCdiProducer.Qualifier String cdiParam) {

        this.pathParam = pathParam;
        this.queryParam = queryParam;
        this.matrixParam = matrixParam;
        this.headerParam = headerParam;
        this.cdiParam = cdiParam;
    }

    /**
     * Provide string representation of a single injected parameter
     * given by the actual path parameter (that is also injected).
     *
     * @return a single parameter value.
     */
    @GET
    public String getParameter() {

        switch (pathParam) {

            case "pathParam": return pathParam;
            case "queryParam": return queryParam;
            case "matrixParam": return matrixParam;
            case "headerParam": return headerParam;
            case "cdiParam": return cdiParam;

            default: throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
