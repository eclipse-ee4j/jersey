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

package org.glassfish.jersey.test.grizzly.web;

import java.io.IOException;
import java.util.EnumSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.glassfish.grizzly.servlet.HttpServletRequestImpl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Reproducer for JERSEY-1893.
 *
 * This is to make sure filters could be utilized even for filtering
 * requests that are being forwarded/included within the server side
 * using {@link javax.servlet.RequestDispatcher} mechanism.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class GrizzlyRequestDispatchFilterTest extends JerseyTest {

    /**
     * We can only register a single Servlet instance. This one then serves as a request dispatcher
     * as well as Jersey Servlet container.
     */
    public static class RequestDispatcherServlet extends ServletContainer {

        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (isInbound(req)) {
                final String action = req.getParameter("action");
                if ("forward".equals(action)) {
                    getServletContext().getRequestDispatcher("/forward").forward(req, resp);
                } else if ("include".equals(action)) {
                    getServletContext().getRequestDispatcher("/included").include(req, resp);
                } else {
                    super.service(req, resp);
                }
            } else {
                super.service(req, resp);
            }
        }

        private boolean isInbound(HttpServletRequest req) {
            // this is a workaround for broken getDispatchType in grizzly
            return req instanceof HttpServletRequestImpl;
        }

    }

    /**
     * Filter that should be applied for regular requests coming directly from client.
     */
    public static class RegularFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {
            servletResponse.getOutputStream().print("[");
            filterChain.doFilter(servletRequest, servletResponse);
            servletResponse.getOutputStream().print("]");
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * Filter that will only be applied for internally forwarded requests.
     */
    public static class ForwardFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {
            servletResponse.getOutputStream().print(">>");
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * Filter for internal include calls.
     */
    public static class IncludeFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {
            servletResponse.getOutputStream().print("SOMETHING ");
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * Resource to be accessed directly from the client only.
     */
    @Path("direct")
    public static class DirectResource {

        @GET
        public String get() {
            return "DIRECT";
        }
    }

    /**
     * Resource that will also be called from the server side using the Servlet forward mechanism.
     */
    @Path("forward")
    public static class ForwardResource {

        @GET
        public String get() {
            return "FORWARD";
        }
    }

    /**
     * Resource that will also be called from the server side using the Servlet include mechanism.
     */
    @Path("included")
    public static class IncludeResource {

        @GET
        public String get() {
            return "INCLUDED";
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.forServlet(RequestDispatcherServlet.class)
                .addFilter(ForwardFilter.class, "forwardFilter",
                        EnumSet.of(javax.servlet.DispatcherType.FORWARD))
                .addFilter(IncludeFilter.class, "includeFilter",
                        EnumSet.of(javax.servlet.DispatcherType.INCLUDE))
                .addFilter(RegularFilter.class, "regularFilter")
                .initParam(ServerProperties.PROVIDER_PACKAGES, this.getClass().getPackage().getName())
                .build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    /**
     * Reproducer test for JERSEY-1893.
     */
    @Test
    public void testGet() {
        WebTarget target = target();

        String s;

        // check that the regular filter gets involved
        s = target.path("direct").request().get(String.class);
        Assert.assertEquals("[DIRECT]", s);

        // the regular filter should work for directly requested forward resource as well.
        s = target.path("forward").request().get(String.class);
        Assert.assertEquals("[FORWARD]", s);

        // forward action should enforce forward filter to be invoked
        s = target.queryParam("action", "forward").request().get(String.class);
        Assert.assertEquals(">>FORWARD", s);

        // direct call to the include resource
        s = target.path("included").request().get(String.class);
        Assert.assertEquals("[INCLUDED]", s);

        // include call should involve both regular and include filter
        s = target.path("included").queryParam("action", "include").request().get(String.class);
        Assert.assertEquals("[SOMETHING INCLUDED]", s);
    }
}
