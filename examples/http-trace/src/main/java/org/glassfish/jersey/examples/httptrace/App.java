/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httptrace;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * This is the example entry point, where Jersey application gets populated and published
 * using the Grizzly 2 HTTP container.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:9998/base/");
    /**
     * Programmatic tracing root resource path.
     */
    public static final String ROOT_PATH_PROGRAMMATIC = "tracing/programmatic";
    /**
     * Annotated class-based tracing root resource path.
     */
    public static final String ROOT_PATH_ANNOTATED = "tracing/annotated";

    /**
     * Main application entry point.
     *
     * @param args application arguments.
     */
    public static void main(String[] args) {
        try {
            System.out.println("HTTP TRACE Support Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format(
                    "Application started.\n"
                            + "To test TRACE with a programmatic resource, send HTTP TRACE request to:%n  %s%s%n"
                            + "To test TRACE with an annotated resource, send HTTP TRACE request to:%n  %s%s%n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH_PROGRAMMATIC,
                    BASE_URI, ROOT_PATH_ANNOTATED));

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
        final ResourceConfig resourceConfig = new ResourceConfig(TracingResource.class);

        final Resource.Builder resourceBuilder = Resource.builder(ROOT_PATH_PROGRAMMATIC);
        resourceBuilder.addMethod(TRACE.NAME).handledBy(new Inflector<ContainerRequestContext, Response>() {

            @Override
            public Response apply(ContainerRequestContext request) {
                if (request == null) {
                    return Response.noContent().build();
                } else {
                    return Response.ok(Stringifier.stringify((ContainerRequest) request), MediaType.TEXT_PLAIN).build();
                }
            }
        });

        return resourceConfig.registerResources(resourceBuilder.build());
    }
}
