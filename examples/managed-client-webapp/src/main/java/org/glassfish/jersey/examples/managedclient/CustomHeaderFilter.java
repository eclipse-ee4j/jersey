/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclient;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A filter for appending and validating custom headers.
 * <p>
 * On the client side, appends a new custom request header with a configured name and value to each outgoing request.
 * </p>
 * <p>
 * On the server side, validates that each request has a custom header with a configured name and value.
 * If the validation fails a HTTP 403 response is returned.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class CustomHeaderFilter implements ContainerRequestFilter, ClientRequestFilter {
    private final String headerName;
    private final String headerValue;

    public CustomHeaderFilter(String headerName, String headerValue) {
        if (headerName == null || headerValue == null) {
            throw new IllegalArgumentException("Header name and value must not be null.");
        }
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException { // validate
        if (!headerValue.equals(ctx.getHeaderString(headerName))) {
            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(String.format("Expected header '%s' not present or value not equal to '%s'", headerName, headerValue))
                    .build());
        }
    }

    @Override
    public void filter(ClientRequestContext ctx) throws IOException { // append
        ctx.getHeaders().putSingle(headerName, headerValue);
    }
}
