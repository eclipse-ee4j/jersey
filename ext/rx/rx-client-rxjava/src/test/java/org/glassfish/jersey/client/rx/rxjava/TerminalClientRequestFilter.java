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

package org.glassfish.jersey.client.rx.rxjava;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author Michal Gajdos
 */
class TerminalClientRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        // Obtain entity - from request or create new.
        final ByteArrayInputStream entity = new ByteArrayInputStream(
                requestContext.hasEntity() ? requestContext.getEntity().toString().getBytes() : "NO-ENTITY".getBytes()
        );

        final int responseStatus = requestContext.getHeaders().getFirst("Response-Status") != null
                ? (int) requestContext.getHeaders().getFirst("Response-Status") : 200;
        Response.ResponseBuilder response = Response.status(responseStatus)
                .entity(entity)
                .type("text/plain")
                // Test properties.
                .header("Test-Thread", Thread.currentThread().getName())
                .header("Test-Uri", requestContext.getUri().toString())
                .header("Test-Method", requestContext.getMethod());

        // Request headers -> Response headers (<header> -> Test-Header-<header>)
        for (final MultivaluedMap.Entry<String, List<String>> entry : requestContext.getStringHeaders().entrySet()) {
            response = response.header("Test-Header-" + entry.getKey(), entry.getValue());
        }

        // Request properties -> Response headers (<header> -> Test-Property-<header>)
        for (final String property : requestContext.getPropertyNames()) {
            response = response.header("Test-Property-" + property, requestContext.getProperty(property));
        }

        requestContext.abortWith(response.build());
    }
}
