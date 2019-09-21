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

package org.glassfish.jersey.client.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * Simple client-side filter that adds X-Requested-By headers to all state-changing
 * request (i.e. request for methods other than GET, HEAD and OPTIONS).
 * This is to satisfy the requirements of the {@code org.glassfish.jersey.server.filter.CsrfProtectionFilter}
 * on the server side.
 *
 * @author Martin Matula
 */
public class CsrfProtectionFilter implements ClientRequestFilter {

    /**
     * Name of the header this filter will attach to the request.
     */
    public static final String HEADER_NAME = "X-Requested-By";

    private static final Set<String> METHODS_TO_IGNORE;

    static {
        HashSet<String> mti = new HashSet<String>();
        mti.add("GET");
        mti.add("OPTIONS");
        mti.add("HEAD");
        METHODS_TO_IGNORE = Collections.unmodifiableSet(mti);
    }

    private final String requestedBy;

    /**
     * Creates a new instance of the filter with X-Requested-By header value set to empty string.
     */
    public CsrfProtectionFilter() {
        this("");
    }

    /**
     * Initialized the filter with a desired value of the X-Requested-By header.
     *
     * @param requestedBy Desired value of X-Requested-By header the filter
     *                    will be adding for all potentially state changing requests.
     */
    public CsrfProtectionFilter(final String requestedBy) {
        this.requestedBy = requestedBy;
    }

    @Override
    public void filter(ClientRequestContext rc) throws IOException {
        if (!METHODS_TO_IGNORE.contains(rc.getMethod()) && !rc.getHeaders().containsKey(HEADER_NAME)) {
            rc.getHeaders().add(HEADER_NAME, requestedBy);
        }
    }
}
