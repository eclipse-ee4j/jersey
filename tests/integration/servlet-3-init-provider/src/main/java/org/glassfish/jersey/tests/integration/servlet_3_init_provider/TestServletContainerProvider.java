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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.internal.spi.RequestScopedInitializerProvider;
import org.glassfish.jersey.servlet.internal.spi.ServletContainerProvider;

/**
 * This is just test purpose implementation of Jersey internal SPI {@link ServletContainerProvider}.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class TestServletContainerProvider implements ServletContainerProvider {

    public static final String TEST_FILTER = "TestFilter";

    private static Set<String> SERVLET_NAMES;
    private static boolean immutableServletNames = false;

    @Override
    public void preInit(final ServletContext servletContext, final Set<Class<?>> classes) throws ServletException {
        classes.add(AbstractHelloWorldResource.class);
    }

    @Override
    public void postInit(final ServletContext servletContext, final Set<Class<?>> classes, final Set<String> servletNames)
            throws ServletException {
        try {
            servletNames.add("TEST");
        } catch (final UnsupportedOperationException ex) {
            TestServletContainerProvider.setImmutableServletNames(true);
        }
    }

    @Override
    public void onRegister(final ServletContext servletContext, final Set<String> servletNames) throws ServletException {
        TestServletContainerProvider.setServletNames(servletNames);

        servletContext.addFilter(TEST_FILTER, TestFilter.class)
                .addMappingForServletNames(EnumSet.allOf(DispatcherType.class), false,
                        servletNames.toArray(new String[servletNames.size()]));
    }

    @Override
    public void configure(final ResourceConfig resourceConfig) throws ServletException {
        if (!resourceConfig.isRegistered(TestContainerLifecycleListener.class)) {
            resourceConfig.register(TestContainerLifecycleListener.class);
        }
    }

    public static Set<String> getServletNames() {
        return SERVLET_NAMES;
    }

    public static boolean isImmutableServletNames() {
        return immutableServletNames;
    }

    private static void setServletNames(final Set<String> servletNames) {
        TestServletContainerProvider.SERVLET_NAMES = servletNames;
    }

    public static void setImmutableServletNames(final boolean immutableServletNames) {
        TestServletContainerProvider.immutableServletNames = immutableServletNames;
    }
}
