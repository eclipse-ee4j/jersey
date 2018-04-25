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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * This is the example entry point, where Jersey application for the example
 * gets populated and published using the Grizzly 2 HTTP container.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    /**
     * "Hello World" root resource path.
     */
    public static final String ROOT_PATH = "helloworld";

    /**
     * Main application entry point.
     *
     * @param args application arguments.
     */
    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(
                    String.format("Application started.%n"
                    + "Try out %s%s%n"
                    + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Test assertion indicator that a GET method handler has been called.
     */
    public static volatile boolean getMethodCalled = false;
    /**
     * Test assertion indicator that a HEAD method handler has been called.
     */
    public static volatile boolean headMethodCalled = false;

    /**
     * Create example application resource configuration.
     *
     * @return initialized resource configuration of the example application.
     */
    public static ResourceConfig create() {
        final Resource.Builder resourceBuilder = Resource.builder(ROOT_PATH);

        resourceBuilder.addMethod("GET").handledBy(new Inflector<ContainerRequestContext, Response>() {

                    @Override
                    public Response apply(ContainerRequestContext data) {
                        getMethodCalled = true;
                        return Response.ok("Hello World!").build();
                    }
                });

        Inflector<ContainerRequestContext, Response> noContentResponder = new Inflector<ContainerRequestContext, Response>() {

            @Override
            public Response apply(ContainerRequestContext data) {
                headMethodCalled = true;
                return Response.noContent().build();
            }
        };
        resourceBuilder.addMethod("HEAD").handledBy(noContentResponder);
        resourceBuilder.addMethod("OPTIONS").handledBy(noContentResponder);

        return new ResourceConfig().registerResources(resourceBuilder.build());
    }
}
