/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclientsimple.resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.Uri;

/**
 * A resource which use managed client injected by {@link org.glassfish.jersey.server.Uri @Uri annotation} to query
 * external resources and resource from {@link StandardResource}.
 *
 * @author Miroslav Fuksa
 *
 */
@Path("client")
public class ClientResource {

    /**
     * Make request to external web site using injected client. The response from the injected client is then
     * returned as a response from this resource method.
     *
     * @param webTarget Injected web target.
     * @return Response.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("glassfish")
    public Response glassfish(@Uri("http://glassfish.java.net/") WebTarget webTarget) {
        final Response response = webTarget.request().get();
        return Response.fromResponse(response).build();
    }

    /**
     * Query {@link StandardResource} and return result based on the results from methods of the {@link StandardResource}.
     *
     * @param dogWebTarget Injected client.
     * @param catWebTarget Injected client.
     * @param elefantWebTarget Injected client.
     * @return String entity.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("animals")
    public String animals(@Uri("resource/dog") WebTarget dogWebTarget,
                          @Uri("resource/cat") WebTarget catWebTarget,
                          @Uri("resource/elefant") WebTarget elefantWebTarget) {

        final String dog = dogWebTarget.request().get(String.class);
        final String cat = catWebTarget.request().get(String.class);
        final String elefant = elefantWebTarget.request().get(String.class);
        return "Queried animals: " + dog + " and " + cat + " and " + elefant;
    }

    /**
     * Query {@link StandardResource} using a injected client. The client injection is using a template parameter {@code id}
     * which is filled by JAX-RS implementation using a path parameter of this resource method.
     *
     * @param webTarget Injected client.
     * @param id Path parameter.
     * @return String entity.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("car/{id}")
    public String car(@Uri("resource/car/{id}") WebTarget webTarget, @PathParam("id") String id) {
        final Response response = webTarget.request().get();
        return "Response from resource/car/" + id + ": " + response.readEntity(String.class);
    }
}
