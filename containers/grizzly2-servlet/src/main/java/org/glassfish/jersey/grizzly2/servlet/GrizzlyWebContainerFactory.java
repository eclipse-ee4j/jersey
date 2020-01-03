/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.Servlet;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.uri.UriComponent;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;

/**
 * Factory for creating and starting Grizzly 2 {@link HttpServer} instances
 * for deploying a {@code Servlet}.
 * <p/>
 * The default deployed server is an instance of {@link ServletContainer}.
 * <p/>
 * If no initialization parameters are declared (or is null) then root
 * resource and provider classes will be found by searching the classes
 * referenced in the java classpath.
 *
 * @author Paul Sandoz
 * @author Pavel Bucek
 */
public final class GrizzlyWebContainerFactory {

    private GrizzlyWebContainerFactory() {
    }

    /**
     * Create a {@link HttpServer} that registers the {@link ServletContainer}.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *          equal to "http". The URI user information and host
     *          are ignored If the URI port is not present then port 80 will be
     *          used. The URI query and fragment components are ignored. Only first path segment will be used
     *          as context path, the rest will be ignored.
     * @return the http server, with the endpoint started.
     *
     * @throws java.io.IOException      if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(String u)
            throws IOException, IllegalArgumentException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        return create(URI.create(u));
    }

    /**
     * Create a {@link HttpServer} that registers the {@link ServletContainer}.
     *
     * @param u          the URI to create the http server. The URI scheme must be
     *                   equal to "http". The URI user information and host
     *                   are ignored If the URI port is not present then port 80 will be
     *                   used. The URI query and fragment components are ignored. Only first path segment will be used
     *                   as context path, the rest will be ignored.
     * @param initParams the servlet initialization parameters.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(String u, Map<String, String> initParams)
            throws IOException, IllegalArgumentException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        return create(URI.create(u), initParams);
    }

    /**
     * Create a {@link HttpServer} that registers the {@link ServletContainer}.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *          equal to "http". The URI user information and host
     *          are ignored If the URI port is not present then port 80 will be
     *          used. The URI query and fragment components are ignored. Only first path segment will be used
     *          as context path, the rest will be ignored.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(URI u)
            throws IOException, IllegalArgumentException {
        return create(u, ServletContainer.class);
    }

    /**
     * Create a {@link HttpServer} that registers the {@link ServletContainer}.
     *
     * @param u          the URI to create the http server. The URI scheme must be
     *                   equal to "http". The URI user information and host
     *                   are ignored If the URI port is not present then port 80 will be
     *                   used. The URI query and fragment components are ignored. Only first path segment will be used
     *                   as context path, the rest will be ignored.
     * @param initParams the servlet initialization parameters.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(URI u,
                                    Map<String, String> initParams) throws IOException {
        return create(u, ServletContainer.class, initParams);
    }

    /**
     * Create a {@link HttpServer} that registers the declared
     * servlet class.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *          equal to "http". The URI user information and host
     *          are ignored If the URI port is not present then port 80 will be
     *          used. The URI query and fragment components are ignored. Only first path segment will be used
     *          as context path, the rest will be ignored.
     * @param c the servlet class.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(String u, Class<? extends Servlet> c) throws IOException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        return create(URI.create(u), c);
    }

    /**
     * Create a {@link HttpServer} that registers the declared
     * servlet class.
     *
     * @param u          the URI to create the http server. The URI scheme must be
     *                   equal to "http". The URI user information and host
     *                   are ignored If the URI port is not present then port 80 will be
     *                   used. The URI query and fragment components are ignored. Only first path segment will be used
     *                   as context path, the rest will be ignored.
     * @param c          the servlet class.
     * @param initParams the servlet initialization parameters.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(String u, Class<? extends Servlet> c,
                                    Map<String, String> initParams) throws IOException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        return create(URI.create(u), c, initParams);
    }

    /**
     * Create a {@link HttpServer} that registers the declared
     * servlet class.
     *
     * @param u the URI to create the http server. The URI scheme must be
     *          equal to "http". The URI user information and host
     *          are ignored If the URI port is not present then port 80 will be
     *          used. The URI query and fragment components are ignored. Only first path segment will be used
     *          as context path, the rest will be ignored.
     * @param c the servlet class
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(URI u, Class<? extends Servlet> c) throws IOException {
        return create(u, c, null);
    }

    /**
     * Create a {@link HttpServer} that registers the declared
     * servlet class.
     *
     * @param u          the URI to create the http server. The URI scheme must be
     *                   equal to "http". The URI user information and host
     *                   are ignored If the URI port is not present then port 80 will be
     *                   used. The URI query and fragment components are ignored. Only first path segment will be used
     *                   as context path, the rest will be ignored.
     * @param c          the servlet class
     * @param initParams the servlet initialization parameters.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(URI u, Class<? extends Servlet> c,
                                    Map<String, String> initParams) throws IOException {
        return create(u, c, null, initParams, null);
    }

    private static HttpServer create(URI u, Class<? extends Servlet> c, Servlet servlet,
                                     Map<String, String> initParams, Map<String, String> contextInitParams)
            throws IOException {
        if (u == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        String path = u.getPath();
        if (path == null) {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ", must be non-null");
        } else if (path.isEmpty()) {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ", must be present");
        } else if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("The URI path, of the URI " + u + ". must start with a '/'");
        }

        path = String.format("/%s", UriComponent.decodePath(u.getPath(), true).get(1).toString());

        WebappContext context = new WebappContext("GrizzlyContext", path);
        ServletRegistration registration;
        if (c != null) {
            registration = context.addServlet(c.getName(), c);
        } else {
            registration = context.addServlet(servlet.getClass().getName(), servlet);
        }
        registration.addMapping("/*");

        if (contextInitParams != null) {
            for (Map.Entry<String, String> e : contextInitParams.entrySet()) {
                context.setInitParameter(e.getKey(), e.getValue());
            }
        }

        if (initParams != null) {
            registration.setInitParameters(initParams);
        }

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(u);
        context.deploy(server);
        return server;
    }

    /**
     * Create a {@link HttpServer} that registers the declared servlet instance.
     *
     * @param u                 the URI to create the http server. The URI scheme must be
     *                          equal to "http". The URI user information and host
     *                          are ignored If the URI port is not present then port 80 will be
     *                          used. The URI query and fragment components are ignored. Only first path segment will be used
     *                          as context path, the rest will be ignored.
     * @param servlet           the servlet instance.
     * @param initParams        the servlet initialization parameters.
     * @param contextInitParams the servlet context initialization parameters.
     * @return the http server, with the endpoint started.
     *
     * @throws IOException              if an error occurs creating the container.
     * @throws IllegalArgumentException if {@code u} is {@code null}.
     */
    public static HttpServer create(URI u, Servlet servlet, Map<String, String> initParams, Map<String, String> contextInitParams)
            throws IOException {
        if (servlet == null) {
            throw new IllegalArgumentException("The servlet must not be null");
        }
        return create(u, null, servlet, initParams, contextInitParams);
    }
}
