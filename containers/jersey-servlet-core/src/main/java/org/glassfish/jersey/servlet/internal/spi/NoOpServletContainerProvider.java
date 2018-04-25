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

import java.lang.reflect.Type;
import java.util.Set;

import javax.ws.rs.core.GenericType;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Basic {@link ExtendedServletContainerProvider} that provides
 * dummy no-op method implementation. It should be convenient to extend if you only need to implement
 * a subset of the original SPI methods.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class NoOpServletContainerProvider implements ExtendedServletContainerProvider {

    public final Type HTTP_SERVLET_REQUEST_TYPE = (new GenericType<Ref<HttpServletRequest>>() { }).getType();
    public final Type HTTP_SERVLET_RESPONSE_TYPE = (new GenericType<Ref<HttpServletResponse>>() { }).getType();

    @Override
    public void preInit(final ServletContext servletContext, final Set<Class<?>> classes) throws ServletException {
        // no-op
    }

    @Override
    public void postInit(
            final ServletContext servletContext, final Set<Class<?>> classes, final Set<String> servletNames) {
        // no-op
    }

    @Override
    public void onRegister(
            final ServletContext servletContext, final Set<String> servletNames) throws ServletException {
        // no-op
    }

    @Override
    public void configure(final ResourceConfig resourceConfig) throws ServletException {
        // no-op
    }

    @Override
    public boolean bindsServletRequestResponse() {
        return false;
    }

    @Override
    public RequestScopedInitializerProvider getRequestScopedInitializerProvider() {
        return null;
    }
}
