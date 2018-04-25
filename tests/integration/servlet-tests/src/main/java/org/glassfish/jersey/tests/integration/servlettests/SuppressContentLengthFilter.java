/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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
 */package org.glassfish.jersey.tests.integration.servlettests;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * JERSEY-2936 reproducer filter.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class SuppressContentLengthFilter implements Filter {

    public static final String PARAMETER_NAME_SUPPRESS_CONTENT_LENGTH = "SuppressContentLength";

    public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (Boolean.parseBoolean(request.getParameter(PARAMETER_NAME_SUPPRESS_CONTENT_LENGTH))) {
            response = new HttpServletResponseWrapper((HttpServletResponse) response) {
                @Override
                public void setContentLength(int len) {
                    // do not delegate to original ServletResponse -> response is NOT committed
                }
            };
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) {
        //NOOP
    }

    public void destroy() {
        //NOOP
    }

}
