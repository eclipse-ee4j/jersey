/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

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
import org.glassfish.jersey.server.model.ResourceMethod;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * This is the example entry point, where Jersey application for the example
 * gets populated and published using the Grizzly 2 HTTP container.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    /**
     * Clipboard root resource path.
     */
    public static final String ROOT_PATH = "clipboard";

    /**
     * Main application entry point.
     *
     * @param args application arguments.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Clipboard Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, createApp(), false);
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
     * Create example application resource configuration.
     *
     * @return initialized resource configuration of the example application.
     */
    public static ResourceConfig createApp() {

        MediaType[] jsonAndTextTypes = new MediaType[] {MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE};

        final ResourceConfig resourceConfig = new ResourceConfig(
                ClipboardResource.class,
                ClipboardDataProvider.ApplicationJson.class,
                ClipboardDataProvider.TextPlain.class);

        final Resource.Builder resourceBuilder = Resource.builder("echo");
        ResourceMethod.Builder rmBuilder = resourceBuilder.addMethod("POST");
        rmBuilder.consumes(jsonAndTextTypes).produces(jsonAndTextTypes)
                .handledBy(new Inflector<ContainerRequestContext, Response>() {

                    @Override
                    public Response apply(ContainerRequestContext request) {
                        ClipboardData data = (request != null)
                                ? ((ContainerRequest) request).readEntity(ClipboardData.class) : null;
                        return Response.ok(data).build();
                    }
                });

        resourceConfig.registerResources(resourceBuilder.build());
        return resourceConfig;
    }
}
