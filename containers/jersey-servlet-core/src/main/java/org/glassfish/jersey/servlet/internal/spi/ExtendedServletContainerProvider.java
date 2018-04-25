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

package org.glassfish.jersey.servlet.internal.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;

/**
 * Implementations could provide their own {@link HttpServletRequest} and {@link HttpServletResponse}
 * binding implementation in HK2 locator and also an implementation of {@link RequestScopedInitializer}
 * that is used to set actual request/response references in injection manager within each request.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @since 2.21
 */
public interface ExtendedServletContainerProvider extends ServletContainerProvider {

    /**
     * Give me a {@link RequestScopedInitializerProvider} instance, that will be utilized
     * at runtime to set the actual HTTP Servlet request and response.
     *
     * The provider returned will be used at runtime for every and each incoming request
     * so that the actual request/response instances could be made accessible
     * from Jersey injection manager.
     *
     * @return request scoped initializer provider.
     */
    public RequestScopedInitializerProvider getRequestScopedInitializerProvider();

    /**
     * Used by Jersey runtime to tell if the extension covers HTTP Servlet request response
     * handling with respect to underlying injection manager.
     *
     * Return {@code true}, if your implementation configures HK2 bindings
     * for {@link HttpServletRequest} and {@link HttpServletResponse}
     * in {@link #configure(ResourceConfig)} method
     * and also provides a {@link RequestScopedInitializer} implementation
     * via {@link #getRequestScopedInitializerProvider()}.
     *
     * @return {@code true} if the extension fully covers HTTP request/response handling.
     */
    public boolean bindsServletRequestResponse();
}
