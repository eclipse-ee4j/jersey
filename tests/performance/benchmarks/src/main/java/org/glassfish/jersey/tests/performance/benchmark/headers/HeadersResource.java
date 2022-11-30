/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark.headers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/headers")
public class HeadersResource {
    public static final String MEDIA_PLAIN = "myapplication/someplaintext+someothertoolongtype";
    public static final String MEDIA_JSON = "myapplication/somejson+someradiculouslylongtype";

    public static final String CONTENT_PLAIN = "some plain text content does not matter";
    public static final String CONTENT_JSON = "\"" + CONTENT_PLAIN + "\"";

    @GET
    @Produces(MEDIA_PLAIN)
    @Path("getPlain")
    public String getMediaPlain() {
        return CONTENT_PLAIN;
    }

    @POST
    @Produces(MEDIA_PLAIN)
    @Consumes(MEDIA_PLAIN)
    @Path("postPlain")
    public String postMediaPlain(String content) {
        if (!CONTENT_PLAIN.equals(content)) {
            throw new WebApplicationException(Response.Status.EXPECTATION_FAILED);
        }
        return CONTENT_PLAIN;
    }

    @GET
    @Produces(MEDIA_JSON)
    @Path("getJson")
    public Response getJson() {
        return Response.ok(CONTENT_PLAIN, MEDIA_JSON).build();
    }

    @POST
    @Produces(MEDIA_JSON)
    @Consumes(MEDIA_JSON)
    @Path("postJson")
    public Response postJson(String json) {
        if (!CONTENT_PLAIN.equals(json)) {
            throw new WebApplicationException(Response.Status.EXPECTATION_FAILED);
        }
        return Response.ok(CONTENT_PLAIN, MEDIA_JSON).build();
    }

}
