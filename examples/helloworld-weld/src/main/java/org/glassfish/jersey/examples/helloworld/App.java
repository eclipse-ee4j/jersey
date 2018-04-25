/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.jboss.weld.environment.se.Weld;

/**
 * Main Java application. Used to bootstrap Weld container and start Grizzly HTTP container.
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/weld/");
    public static final String ROOT_PATH = "application.wadl";

    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey Example Weld App");

            final Weld weld = new Weld();
            weld.initialize();

            final ResourceConfig resourceConfig = createJaxRsApp();

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                    weld.shutdown();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C",
                    BASE_URI, ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * JAX-RS application defined as a CDI bean.
     */
    @ManagedBean
    public static class JaxRsApplication extends Application {

        @Context
        UriInfo uInfo;

        static final Set<Class<?>> appClasses = new HashSet<>();

        static {
            appClasses.add(HelloWorldResource.class);
            appClasses.add(AppScopedResource.class);
            appClasses.add(RequestScopedResource.class);
            appClasses.add(CustomInterceptor.class);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return appClasses;
        }
    }

    /**
     * Create JAX-RS application. The same one is used also in the tests.
     *
     * @return Jersey's resource configuration of the Weld application.
     */
    public static ResourceConfig createJaxRsApp() {
        return ResourceConfig.forApplicationClass(JaxRsApplication.class);
    }
}
