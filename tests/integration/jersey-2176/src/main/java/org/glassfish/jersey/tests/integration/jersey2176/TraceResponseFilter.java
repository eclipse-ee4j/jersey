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

package org.glassfish.jersey.tests.integration.jersey2176;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class TraceResponseFilter implements Filter {

    public static final String X_SERVER_DURATION_HEADER = "X-SERVER-DURATION";
    public static final String X_STATUS_HEADER = "X-STATUS";
    public static final String X_NO_FILTER_HEADER = "X-NO-FILTER";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        TraceResponseWrapper wrappedResponse = null;
        if (((HttpServletRequest) request).getHeader(X_NO_FILTER_HEADER) == null) {
            response = wrappedResponse = new TraceResponseWrapper((HttpServletResponse) response);
        }
        String status = "n/a";
        final long startTime = System.nanoTime();
        try {
            chain.doFilter(request, response);
            status = "OK";
        } catch (final Throwable th) {
            status = "FAIL";
        } finally {
            final long duration = System.nanoTime() - startTime;
            ((HttpServletResponse) response).addHeader(X_SERVER_DURATION_HEADER, String.valueOf(duration));
            ((HttpServletResponse) response).addHeader(X_STATUS_HEADER, status);
            if (wrappedResponse != null) {
                ((HttpServletResponse) response).setHeader(HttpHeaders.CONTENT_LENGTH, wrappedResponse.getContentLength());
                wrappedResponse.writeBodyAndClose(response.getCharacterEncoding());
            }
        }
    }

}
