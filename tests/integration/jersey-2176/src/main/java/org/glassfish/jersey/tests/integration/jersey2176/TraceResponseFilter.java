/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author Libor Kramolis
 */
public class TraceResponseFilter extends HttpFilter {

    public static final String X_SERVER_DURATION_HEADER = "X-SERVER-DURATION";
    public static final String X_STATUS_HEADER = "X-STATUS";
    public static final String X_NO_FILTER_HEADER = "X-NO-FILTER";

    private static final Logger LOG = System.getLogger(TraceResponseFilter.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final TraceResponseWrapper wrappedResponse;
        if (request.getHeader(X_NO_FILTER_HEADER) != null) {
            wrappedResponse = null;
        } else {
            wrappedResponse = new TraceResponseWrapper(response);
        }
        String status = "n/a";
        final long startTime = System.nanoTime();
        try {
            chain.doFilter(request, wrappedResponse == null ? response : wrappedResponse);
            status = "OK";
        } catch (final Throwable th) {
            status = "FAIL";
        } finally {
            int httpStatus = response.getStatus();
            if (httpStatus == 500) {
                status = "FAIL";
            }
            final long duration = System.nanoTime() - startTime;
            LOG.log(Level.INFO, "Status: " + status + ", http status: " + httpStatus + ", duration: " + duration + " ns");
            if (!response.isCommitted()) {
                response.addHeader(X_SERVER_DURATION_HEADER, String.valueOf(duration));
                response.addHeader(X_STATUS_HEADER, status);
                if (wrappedResponse != null) {
                    response.setHeader(HttpHeaders.CONTENT_LENGTH, wrappedResponse.getContentLength());
                    wrappedResponse.writeBodyAndClose(response.getCharacterEncoding());
                }
            }
        }
    }

}
