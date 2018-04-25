/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.oauth1;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * {@link WebApplicationException Web application exception} that is mapped either
 * to {@link javax.ws.rs.core.Response.Status#BAD_REQUEST} (e.g. if problem with OAuth
 * parameters occurs) or
 * {@link javax.ws.rs.core.Response.Status#UNAUTHORIZED} (e.g. if signature is incorrect).
 *
 * @author Martin Matula
 * @author Miroslav Fuksa
 */
public class OAuth1Exception extends WebApplicationException {

    /**
     * Create a new exception.
     * @param status Response status.
     * @param wwwAuthHeader {@code Authorization} header value of the request that cause the exception.
     */
    public OAuth1Exception(final Response.Status status, final String wwwAuthHeader) {
        super(createResponse(status, wwwAuthHeader));
    }

    /**
     * Get the status of the error response.
     *
     * @return Response status code.
     */
    public Response.Status getStatus() {
        return Response.Status.fromStatusCode(super.getResponse().getStatus());
    }

    /**
     * Get the {@code WWW-Authenticate} header of the request that cause the exception.
     *
     * @return {@code WWW-Authenticate} header value.
     */
    public String getWwwAuthHeader() {
        return super.getResponse().getHeaderString(HttpHeaders.WWW_AUTHENTICATE);
    }

    private static Response createResponse(Response.Status status, String wwwAuthHeader) {
        ResponseBuilder rb = Response.status(status);
        if (wwwAuthHeader != null) {
            rb.header(HttpHeaders.WWW_AUTHENTICATE, wwwAuthHeader);
        }
        return rb.build();
    }
}

