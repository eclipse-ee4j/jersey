/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.restclient;

import java.util.List;
import java.util.Map;

import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * Created by David Kral.
 */

@Path("resource")
public interface ApplicationResource {

    @GET
    List<String> getValue();

    @GET
    @Path("map")
    Map<String, String> getTestMap();

    @POST
    String postAppendValue(String value);

    @POST
    @Path("getJson")
    @Consumes(MediaType.APPLICATION_JSON)
    JsonValue someJsonOperation(JsonValue jsonValue);

    @GET
    @Path("stringEntity")
    JsonValue jsonValue();

    default String sayHi() {
        return "Hi";
    }

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("methodContent")
    String methodContentType(@HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType contentType, String entity);

}
