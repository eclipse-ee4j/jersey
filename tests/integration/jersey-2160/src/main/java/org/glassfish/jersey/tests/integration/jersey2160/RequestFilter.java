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

package org.glassfish.jersey.tests.integration.jersey2160;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import javax.servlet.http.HttpServletRequest;

/**
 * Filter that set a property on actual request. The property value
 * is expected to be propagated to the resource method via injected
 * {@link HttpServletRequest} parameter.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@PreMatching
public class RequestFilter implements ContainerRequestFilter {

    public static final String REQUEST_NUMBER_PROPERTY = "request-number";
    public static final String REQUEST_NUMBER_HEADER = "number";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        final String number = ctx.getHeaderString(REQUEST_NUMBER_HEADER);
        ctx.setProperty(REQUEST_NUMBER_PROPERTY, number);
    }
}
