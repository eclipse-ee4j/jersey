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

package org.glassfish.jersey.tests.integration.servlet_request_wrapper_binding;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.glassfish.jersey.servlet.internal.spi.NoOpServletContainerProvider;
import org.glassfish.jersey.servlet.internal.spi.RequestScopedInitializerProvider;

/**
 * Servlet container provider that wraps the original Servlet request/response.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class RequestResponseWrapperProvider extends NoOpServletContainerProvider {

    /**
     * Subclass standard wrapper so that we make 100 % sure we are getting the right type.
     */
    public static class RequestWrapper extends HttpServletRequestWrapper {

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }
    }

    /**
     * Subclass standard wrapper so that we make 100 % sure we are getting the right type.
     */
    public static class ResponseWrapper extends HttpServletResponseWrapper {

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }

    @Override
    public RequestScopedInitializerProvider getRequestScopedInitializerProvider() {
        return context -> (RequestScopedInitializer) injectionManager -> {
            injectionManager.<Ref<HttpServletRequest>>getInstance(HTTP_SERVLET_REQUEST_TYPE)
                    .set(wrapped(context.getHttpServletRequest()));
            injectionManager.<Ref<HttpServletResponse>>getInstance(HTTP_SERVLET_RESPONSE_TYPE)
                    .set(wrapped(context.getHttpServletResponse()));
        };
    }

    private HttpServletRequest wrapped(final HttpServletRequest request) {
        return new RequestWrapper(request);
    }

    private HttpServletResponse wrapped(final HttpServletResponse response) {
        return new ResponseWrapper(response);
    }
}
