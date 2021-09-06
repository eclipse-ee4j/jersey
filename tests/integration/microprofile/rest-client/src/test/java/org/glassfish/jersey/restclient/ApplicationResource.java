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

import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

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

    @GET
    @Path("{content: [a-zA-Z]+}")
    @Produces(MediaType.TEXT_PLAIN)
    String regex(@PathParam("content") String content);

    @GET
    @Path("content1/{content1}/content0/{content0: [0-9]{4}}")
    @Produces(MediaType.TEXT_PLAIN)
    String regex0(@PathParam("content1") String context0, @PathParam("content0") String context1);
}
