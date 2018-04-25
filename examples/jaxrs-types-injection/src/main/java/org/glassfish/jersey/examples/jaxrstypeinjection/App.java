/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxrstypeinjection;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Jersey application that demonstrates injection of JAX-RS components into resources.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/jaxrs-type-injection/");
    /**
     * Programmatic root resource path template.
     */
    public static final String ROOT_PATH_PROGRAMMATIC = "programmatic/{p1}/{p2}";
    /**
     * Annotated class-based root resource path template demonstrating instance field injection.
     */
    public static final String ROOT_PATH_ANNOTATED_INSTANCE = "annotated/instance/{p1}/{p2}";
    /**
     * Annotated class-based root resource path template demonstrating method injection.
     */
    public static final String ROOT_PATH_ANNOTATED_METHOD = "annotated/method/{p1}/{p2}";

    /**
     * Main application entry point.
     *
     * @param args application arguments.
     */
    public static void main(String[] args) {
        try {
            System.out.println("JAX-RS Type Injection Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format(
                    "Application started.%n"
                            + "To test injection into a programmatic resource, try out:%n  %s%s%s%n"
                            + "To test instance injection into an annotated resource, try out:%n  %s%s%s%n"
                            + "To test method injection into an annotated resource, try out:%n  %s%s%s%n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH_PROGRAMMATIC, "?q1=<value_1>&q2=<value_2>&q2=<value_3>",
                    BASE_URI, ROOT_PATH_ANNOTATED_INSTANCE, "?q1=<value_1>&q2=<value_2>&q2=<value_3>",
                    BASE_URI, ROOT_PATH_ANNOTATED_METHOD, "?q1=<value_1>&q2=<value_2>&q2=<value_3>"));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create example application resource configuration.
     *
     * @return initialized resource configuration of the example application.
     */
    public static ResourceConfig create() {
        final ResourceConfig resourceConfig = new ResourceConfig(JaxrsInjectionReportingResource.class);
        final Resource.Builder resourceBuilder = Resource.builder(ROOT_PATH_PROGRAMMATIC);
        resourceBuilder.addMethod("GET").handledBy(JaxrsInjectionReportingInflector.class);

        return resourceConfig.registerResources(resourceBuilder.build());
    }
}
