/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclient;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Jersey programmatic managed client example application.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/managedclient/");

    public static void main(String[] args) {
        try {
            System.out.println("\"Managed Client\" Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out public endpoints:\n  %s%s\n  %s%s\n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, "public/a",
                    BASE_URI, "public/b"));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Create JAX-RS application for the example.
     *
     * @return create JAX-RS application for the example.
     */
    public static ResourceConfig create() {
        return new ResourceConfig(PublicResource.class, InternalResource.class, CustomHeaderFeature.class)
                .property(ClientA.class.getName() + ".baseUri", BASE_URI.toString() + "internal");
    }

    public static class MyClientAConfig extends ClientConfig {

        public MyClientAConfig() {
            this.register(new CustomHeaderFilter("custom-header", "a"));
        }
    }

    public static class MyClientBConfig extends ClientConfig {

        public MyClientBConfig() {
            this.register(new CustomHeaderFilter("custom-header", "b"));
        }
    }
}
