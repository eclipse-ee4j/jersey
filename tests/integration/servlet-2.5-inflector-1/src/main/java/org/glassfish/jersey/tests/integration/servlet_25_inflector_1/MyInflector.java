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

package org.glassfish.jersey.tests.integration.servlet_25_inflector_1;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.process.Inflector;

/**
 * @author Michal Gajdos
 */
public class MyInflector implements Inflector<ContainerRequestContext, Response> {

    @Inject
    private Provider<HttpServletRequest> requestProvider;
    @Inject
    private Provider<HttpServletResponse> responseProvider;

    @Override
    public Response apply(final ContainerRequestContext requestContext) {
        final StringBuilder stringBuilder = new StringBuilder();

        // Request provider & request.
        if (requestProvider != null) {
            stringBuilder.append("requestProvider_");
            stringBuilder.append(requestProvider.get() != null ? "request" : null);
        } else {
            stringBuilder.append("null_null");
        }

        stringBuilder.append('_');

        // Response provider & response.
        if (responseProvider != null) {
            stringBuilder.append("responseProvider_");
            stringBuilder.append(responseProvider.get() != null ? "response" : null);
        } else {
            stringBuilder.append("null_null");
        }

        return Response.ok(stringBuilder.toString()).build();
    }
}
