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

package org.glassfish.jersey.servlet.internal.spi;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * This is internal Jersey SPI to hook to Jersey servlet initialization process driven by
 * {@code org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer}.
 * The provider implementation class is registered via {@code META-INF/services}.
 *
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.4.1
 */
public interface ServletContainerProvider {

    /**
     * Do your pre-initialization job before Jersey starts its servlet initialization.
     *
     * It is allowed to configure {@link ServletContext} or add/remove servlet registrations.
     * Parameter {@code servletNames} contains list of names of currently registered Jersey servlets.
     *
     * @param servletContext the {@code ServletContext} of the JAX-RS/Jersey web application that is being started.
     * @param classes        the mutable Set of application classes that extend {@link javax.ws.rs.core.Application},
     *                       implement, or have been annotated with the class types {@link javax.ws.rs.Path},
     *                       {@link javax.ws.rs.ext.Provider} or {@link javax.ws.rs.ApplicationPath}.
     *                       May be empty, never {@code null}.
     * @throws ServletException if an error has occurred. {@code javax.servlet.ServletContainerInitializer.onStartup}
     *                          is interrupted.
     */
    public void preInit(ServletContext servletContext, Set<Class<?>> classes) throws ServletException;

    /**
     * Do your post-initialization job after Jersey finished its servlet initialization.
     *
     * It is allowed to configure {@link ServletContext} or add/remove servlet registrations.
     * Parameter {@code servletNames} contains list of names of currently registered Jersey servlets.
     *
     * @param servletContext the {@code ServletContext} of the JAX-RS/Jersey web application that is being started.
     * @param classes        the mutable Set of application classes that extend {@link javax.ws.rs.core.Application},
     *                       implement, or have been annotated with the class types {@link javax.ws.rs.Path},
     *                       {@link javax.ws.rs.ext.Provider} or {@link javax.ws.rs.ApplicationPath}.
     *                       May be empty, never {@code null}.
     * @param servletNames   the Immutable set of Jersey's ServletContainer names. May be empty, never {@code null}.
     * @throws ServletException if an error has occurred. {@code javax.servlet.ServletContainerInitializer.onStartup}
     *                          is interrupted.
     */
    public void postInit(ServletContext servletContext, Set<Class<?>> classes, final Set<String> servletNames)
            throws ServletException;

    /**
     * Notifies the provider about all registered Jersey servlets by its names.
     *
     * It is allowed to configure {@link ServletContext}. Do not add/remove any servlet registrations here.
     *
     * Parameter {@code servletNames} contains list of names of registered Jersey servlets.
     * Currently it is {@link ServletContainer} or
     * {@code org.glassfish.jersey.servlet.portability.PortableServletContainer} servlets.
     *
     * It does not matter servlet container is configured in {@code web.xml},
     * by {@code org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer}
     * or by customer direct Servlet API calls.
     *
     * @param servletContext the {@code ServletContext} of the JAX-RS/Jersey web application that is being started.
     * @param servletNames   the Immutable set of Jersey's ServletContainer names. May be empty, never {@code null}.
     * @throws ServletException if an error has occurred. {@code javax.servlet.ServletContainerInitializer.onStartup}
     *                          is interrupted.
     */
    public void onRegister(ServletContext servletContext, final Set<String> servletNames) throws ServletException;

    /**
     * This method is called for each {@link ServletContainer} instance initialization,
     * i.e. during {@link org.glassfish.jersey.servlet.WebComponent} initialization.
     *
     * The method is also called during {@link ServletContainer#reload()} or
     * {@link ServletContainer#reload(ResourceConfig)} methods invocation.
     *
     * It does not matter servlet container is configured in {@code web.xml},
     * by {@code org.glassfish.jersey.servlet.init.JerseyServletContainerInitializer}
     * or by customer direct Servlet API calls.
     *
     * @param resourceConfig Jersey application configuration.
     * @throws ServletException if an error has occurred. {@code org.glassfish.jersey.servlet.WebComponent} construction
     *                          is interrupted.
     */
    public void configure(ResourceConfig resourceConfig) throws ServletException;
}
