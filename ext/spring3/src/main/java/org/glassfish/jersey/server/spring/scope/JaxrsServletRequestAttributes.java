/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.scope;

import javax.ws.rs.container.ContainerRequestContext;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * JAX-RS based Spring RequestAttributes implementation for Servlet-based applications.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class JaxrsServletRequestAttributes extends ServletRequestAttributes {

    private final ContainerRequestContext requestContext;

    /**
     * Create a new JAX-RS ServletRequestAttributes instance for the given request.
     *
     * @param request        current HTTP request
     * @param requestContext JAX-RS request context
     */
    public JaxrsServletRequestAttributes(final HttpServletRequest request, final ContainerRequestContext requestContext) {
        super(request);
        this.requestContext = requestContext;
    }

    @Override
    public Object resolveReference(String key) {
        if (REFERENCE_REQUEST.equals(key)) {
            return this.requestContext;
        } else if (REFERENCE_SESSION.equals(key)) {
            return super.getSession(true);
        } else {
            return null;
        }
    }
}
