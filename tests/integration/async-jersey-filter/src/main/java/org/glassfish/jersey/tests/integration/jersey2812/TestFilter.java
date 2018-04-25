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
 */

package org.glassfish.jersey.tests.integration.jersey2812;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This servlet filter class provides a means to detect whether Jersey (in servlet filter based setup) properly freed the
 * server-side thread processing the http request to an async RESTful resource where {@link javax.ws.rs.container.AsyncResponse}
 * wasn't resumed.
 * <p/>
 * Reported as JERSEY-2812.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class TestFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(TestFilter.class.getName());
    public static final String CDL_FINISHED = "CDL-finished";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain)
            throws IOException, ServletException {
        LOGGER.finest(new Date() + " Thread " + Thread.currentThread().getName() + " is being acquired...");

        final CountDownLatch cdlFinished = new CountDownLatch(1);
        servletRequest.setAttribute(CDL_FINISHED, cdlFinished);

        filterChain.doFilter(servletRequest, servletResponse);

        // the thread did return from Jersey
        cdlFinished.countDown();

        LOGGER.finest(new Date() + " Thread " + Thread.currentThread().getName() + " is being released.");
    }

    @Override
    public void destroy() {

    }
}
