/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.internal;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.servlet.WebComponent;
import org.glassfish.jersey.servlet.WebConfig;
import org.glassfish.jersey.servlet.WebFilterConfig;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

public class RequestInputStreamTest {
    @Test
    public void test404RequestInputStream() throws ServletException, IOException {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                switch (method.getName()) {
                    case "getHeaderNames":
                        return Collections.emptyEnumeration();
                    case "getInputStream":
                        throw new IllegalStateException("ServletRequest#getInputStream clashes with ServletRequest#getReader");
                }
                return null;
            }
        };

        FilterConfig filterConfig = new FilterConfig() {
            @Override
            public String getFilterName() {
                return null;
            }

            @Override
            public ServletContext getServletContext() {
                return (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{ServletContext.class},
                        handler);
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
        WebConfig dummyWebConfig = new WebFilterConfig(filterConfig);
        ResourceConfig resourceConfig = new ResourceConfig()
                .property(CommonProperties.PROVIDER_DEFAULT_DISABLE, "ALL")
                .property(ServerProperties.WADL_FEATURE_DISABLE, true)
                .property(ServletProperties.FILTER_FORWARD_ON_404, true)
                .property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        WebComponent component = new WebComponent(dummyWebConfig, resourceConfig);
        component.service(URI.create("http://localhost"), URI.create("http://localhost"),
                (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[] {HttpServletRequest.class},
                        handler
                        ),
                (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class[]{HttpServletResponse.class},
                        handler)
                );
    }
}
