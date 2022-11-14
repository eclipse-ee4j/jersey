/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;

/**
 * Context encoding test. See Jersey-4949.
 */
public class ContextPathEncodingTest {
    private static final String PATH = "A%20B";
    private static final String CONTEXT = "c%20ntext";

    @Test
    public void contextEncodingTest() throws ServletException, IOException {
        // In jetty maven plugin, context is set by
        //<configuration>
        //    <scan>10</scan>
        //    <webApp>
        //        <contextPath>/c ntext</contextPath>
        //    </webApp>
        //</configuration>

        //Servlet path is not encoded, context is encoded
        final ServletRequestValues servletRequestValues = new ServletRequestValues(
                "/" + CONTEXT,
                "",
                "/" + CONTEXT + "/" + PATH
        );
        final EncodingTestServletContainer testServletContainer = new EncodingTestServletContainer(
                "/" + CONTEXT + "/",
                "/" + CONTEXT + "/" + PATH
        );
        EncodingTestData testData = new EncodingTestData(servletRequestValues, testServletContainer);

        testData.test();
    }

    @Test
    public void servletPathEncodingTest() throws ServletException, IOException {
        //Servlet path is not encoded, context is encoded
        final ServletRequestValues servletRequestValues = new ServletRequestValues(
                "/",
                "A B",
                "/" + PATH + "/" + PATH
        );
        final EncodingTestServletContainer testServletContainer = new EncodingTestServletContainer(
                "/" + PATH + "/",
                "/" + PATH + "/" + PATH
        );
        EncodingTestData testData = new EncodingTestData(servletRequestValues, testServletContainer);

        testData.test();
    }

    static class EncodingTestData {
        final ServletRequestValues servletRequestValues;
        final EncodingTestServletContainer encodingTestServletContainer;
        final HttpServletRequest httpServletRequest;

        EncodingTestData(ServletRequestValues servletRequestValues, EncodingTestServletContainer encodingTestServletContainer) {
            this.servletRequestValues = servletRequestValues;
            this.encodingTestServletContainer = encodingTestServletContainer;
            this.httpServletRequest = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{HttpServletRequest.class}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                           return servletRequestValues.handle(method.getName());
                        }
                    });
        }

        public void test() throws ServletException, IOException {
            encodingTestServletContainer.service(httpServletRequest, (HttpServletResponse) null);
        }

    }

    static class ServletRequestValues {
        final String servletPath;
        final String requestUri;
        final String contextPath;

        ServletRequestValues(String contextPath, String servletPath, String requestUri) {
            this.servletPath = servletPath;
            this.requestUri = requestUri;
            this.contextPath = contextPath;
        }

        Object handle(String name) {
            switch (name) {
                case "getServletPath":
                    return servletPath;
                case "getRequestURI":
                    return requestUri;
                case "getRequestURL":
                    return new StringBuffer(requestUri);
                case "getContextPath":
                    return contextPath;
                default:
                    return null;
            }
        }
    }

    static class EncodingTestServletContainer extends ServletContainer {
        final String baseUri;
        final String requestUri;

        EncodingTestServletContainer(String baseUri, String requestUri) {
            this.baseUri = baseUri;
            this.requestUri = requestUri;
        }

        @Override
        public Value<Integer> service(URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response) {
            Assertions.assertEquals(this.baseUri, baseUri.toASCIIString());
            Assertions.assertEquals(this.requestUri, requestUri.toASCIIString());
            return Values.of(0);
        }

        //Update visibility
        public void service(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException, IOException {
            super.service(request, response);
        }
    };
}
