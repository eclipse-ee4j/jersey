/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.portability;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Jersey Servlet/Filter class that can be referenced in web.xml instead of Jersey 1.x specific
 * {@code com.sun.jersey.spi.container.servlet.ServletContainer} and Jersey 2.x specific
 * {@link org.glassfish.jersey.servlet.ServletContainer} to enable web application portability between
 * Jersey 1.x and Jersey 2.x servlet containers.
 * <p>
 *     Since for some of the {@link org.glassfish.jersey.servlet.ServletProperties servlet init parameters} that can be
 *     specified in web.xml you may want different values depending on which version of Jersey container is present,
 *     You can prefix the init parameter name either with {@code jersey1#} or {@code jersey2#} to
 *     make it specific to a given version. For example, to specify different values for
 *     {@code javax.ws.rs.Application} init parameter depending on the version of Jersey used, you can include
 *     the following in your web.xml:
 *     <pre>
 *     &lt;servlet&gt;
 *         &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *         &lt;servlet-class&gt;org.glassfish.jersey.servlet.portability.PortableServletContainer&lt;/servlet-class&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;jersey1#javax.ws.rs.Application&lt;/param-name&gt;
 *             &lt;param-value&gt;myapp.jersey1specific.Jersey1Application&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;jersey2#javax.ws.rs.Application&lt;/param-name&gt;
 *             &lt;param-value&gt;myapp.jersey2specific.Jersey2Application&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *     &lt;/servlet&gt;
 *     </pre>
 * </p>
 *
 * @author Martin Matula
 */
public class PortableServletContainer implements Filter, Servlet {

    private static final String JERSEY_1_PREFIX = "jersey1#";
    private static final String JERSEY_2_PREFIX = "jersey2#";

    private final Servlet wrappedServlet;
    private final Filter wrappedFilter;
    private final String includePrefix;
    private final String excludePrefix;

    /**
     * Create a new servlet container.
     */
    @SuppressWarnings("unchecked")
    public PortableServletContainer() {
        Class<Servlet> servletClass;
        boolean isJersey1 = false;
        try {
            servletClass = (Class<Servlet>) Class.forName("com.sun.jersey.spi.container.servlet.ServletContainer");
            isJersey1 = true;
        } catch (ClassNotFoundException e) {
            // Jersey 1.x not present, try Jersey 2.x
            try {
                servletClass = (Class<Servlet>) Class.forName("org.glassfish.jersey.servlet.ServletContainer");
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(LocalizationMessages.JERSEY_NOT_AVAILABLE());
            }
        }

        try {
            wrappedServlet = servletClass.newInstance();
            wrappedFilter = (Filter) wrappedServlet;
        } catch (Exception e) {
            throw new RuntimeException(LocalizationMessages.JERSEY_CONTAINER_CANT_LOAD(), e);
        }
        includePrefix = isJersey1 ? JERSEY_1_PREFIX : JERSEY_2_PREFIX;
        excludePrefix = isJersey1 ? JERSEY_2_PREFIX : JERSEY_1_PREFIX;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        wrappedFilter.init(new FilterConfigWrapper(filterConfig));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        wrappedFilter.doFilter(request, response, chain);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        wrappedServlet.init(new ServletConfigWrapper(config));
    }

    @Override
    public ServletConfig getServletConfig() {
        return wrappedServlet.getServletConfig();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        wrappedServlet.service(req, res);
    }

    @Override
    public String getServletInfo() {
        return wrappedServlet.getServletInfo();
    }

    @Override
    public void destroy() {
        wrappedServlet.destroy();
    }

    private abstract class InitParamsWrapper {

        private final HashMap<String, String> filteredInitParams = new HashMap<String, String>();

        void init() {
            for (Enumeration e = getInitParamNames(); e.hasMoreElements(); ) {
                String name = (String) e.nextElement();
                String value = getInitParamValue(name);
                if (name.startsWith(includePrefix)) {
                    name = name.substring(includePrefix.length());
                } else if (name.startsWith(excludePrefix)) {
                    continue;
                }
                filteredInitParams.put(name, value);
            }
        }

        abstract String getInitParamValue(String name);

        abstract Enumeration getInitParamNames();

        public String getInitParameter(String name) {
            return filteredInitParams.get(name);
        }

        public Enumeration getInitParameterNames() {
            return Collections.enumeration(filteredInitParams.keySet());
        }
    }

    private class FilterConfigWrapper extends InitParamsWrapper implements FilterConfig {

        private final FilterConfig wrapped;

        FilterConfigWrapper(FilterConfig wrapped) {
            this.wrapped = wrapped;
            init();
        }

        @Override
        public String getFilterName() {
            return wrapped.getFilterName();
        }

        @Override
        public ServletContext getServletContext() {
            return wrapped.getServletContext();
        }

        @Override
        String getInitParamValue(String name) {
            return wrapped.getInitParameter(name);
        }

        @Override
        Enumeration getInitParamNames() {
            return wrapped.getInitParameterNames();
        }
    }

    private class ServletConfigWrapper extends InitParamsWrapper implements ServletConfig {

        private final ServletConfig wrapped;

        ServletConfigWrapper(ServletConfig wrapped) {
            this.wrapped = wrapped;
            init();
        }

        @Override
        String getInitParamValue(String name) {
            return wrapped.getInitParameter(name);
        }

        @Override
        Enumeration getInitParamNames() {
            return wrapped.getInitParameterNames();
        }

        @Override
        public String getServletName() {
            return wrapped.getServletName();
        }

        @Override
        public ServletContext getServletContext() {
            return wrapped.getServletContext();
        }
    }
}
