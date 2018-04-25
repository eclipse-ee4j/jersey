/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response.Status;

import javax.annotation.Priority;

/**
 * Simple server-side request filter that implements CSRF protection as per the
 * <a href="http://www.nsa.gov/ia/_files/support/guidelines_implementation_rest.pdf">Guidelines for Implementation of REST</a>
 * by NSA (section IV.F) and
 * section 4.3 of <a href="http://seclab.stanford.edu/websec/csrf/csrf.pdf">this paper</a>.
 * If you add it to the request filters of your application, it will check for X-Requested-By header in each
 * request except for those that don't change state (GET, OPTIONS, HEAD). If the header is not found,
 * it returns {@link Status#BAD_REQUEST} response back to the client.
 *
 * @see org.glassfish.jersey.client.filter.CsrfProtectionFilter
 *
 * @author Martin Matula
 */
@Priority(Priorities.AUTHENTICATION) // should be one of the first post-matching filters to get executed
public class CsrfProtectionFilter implements ContainerRequestFilter {

    /**
     * Name of the header this filter will attach to the request.
     */
    public static final String HEADER_NAME = "X-Requested-By";

    private static final Set<String> METHODS_TO_IGNORE;
    static {
        HashSet<String> mti = new HashSet<>();
        mti.add("GET");
        mti.add("OPTIONS");
        mti.add("HEAD");
        METHODS_TO_IGNORE = Collections.unmodifiableSet(mti);
    }

    @Override
    public void filter(ContainerRequestContext rc) throws IOException {
        if (!METHODS_TO_IGNORE.contains(rc.getMethod()) && !rc.getHeaders().containsKey(HEADER_NAME)) {
            throw new BadRequestException();
        }
    }
}
