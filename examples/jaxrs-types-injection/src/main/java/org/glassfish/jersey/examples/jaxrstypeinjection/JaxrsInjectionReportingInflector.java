/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxrstypeinjection;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.process.Inflector;

/**
 * Programmatic resource.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class JaxrsInjectionReportingInflector implements Inflector<Request, Response> {

    @Inject
    HttpHeaders httpHeaders;
    @Inject
    UriInfo uriInfo;
    @PathParam(value = "p1")
    String p1;
    private PathSegment p2;

    @PathParam(value = "p2")
    public void setP2(PathSegment p2) {
        this.p2 = p2;
    }
    @QueryParam(value = "q1")
    private int q1;
    @QueryParam(value = "q2")
    private List<String> q2;

    @Override
    public Response apply(Request data) {
        StringBuilder sb = ReportBuilder.append(
                new StringBuilder("Injected information:\n"), uriInfo, httpHeaders);
        sb.append("\n URI component injection:");
        sb.append("\n   String path param p1=").append(p1);
        sb.append("\n   PathSegment path param p2=").append(p2);
        sb.append("\n   int query param q1=").append(q1);
        sb.append("\n   List<String> query param q2=").append(q2);
        return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
    }
}
