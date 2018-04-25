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

package org.glassfish.jersey.tests.integration.jersey2176;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Test resource.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Path("/resource")
public class Issue2176ReproducerResource {

    static final String X_FAIL_HEADER = "X-FAIL";
    static final String X_RESPONSE_ENTITY_HEADER = "X-RESPONSE-ENTITY";

    @GET
    @Produces("text/plain")
    @Path("{status}")
    public Response getText(@PathParam("status") int uc,
                            @Context HttpHeaders headers) throws MyException {
        if (uc == -1) {
            throw new MyException("UC= " + uc);
        } else if (uc == -2) {
            throw new IllegalStateException("UC= " + uc);
        } else if (uc == -3) {
            throw new WebApplicationException("UC= " + uc, 321);
        } else if (uc == -4) {
            throw new WebApplicationException("UC= " + uc, 432);
        }

        final Response.ResponseBuilder responseBuilder = Response.status(uc);
        if (headers.getRequestHeaders().containsKey(X_RESPONSE_ENTITY_HEADER)) {
            responseBuilder.entity("ENTITY");
        }
        final Response response = responseBuilder.build();
        if (headers.getRequestHeaders().containsKey(X_RESPONSE_ENTITY_HEADER)) {
            response.getHeaders().add(X_RESPONSE_ENTITY_HEADER, "true");
        }
        if (headers.getRequestHeaders().containsKey(X_FAIL_HEADER)) {
            response.getHeaders().add(X_FAIL_HEADER, "true");
        }
        return response;
    }

}
