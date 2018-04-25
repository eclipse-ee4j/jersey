/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Java application class starting Grizzly2 server with Entity Data Filtering app deployed.
 *
 * @author Michal Gajdos
 */
public final class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    public static void main(String[] args) {
        try {
            System.out.println("Jersey Entity Data Filtering Example.");

            final HttpServer server = GrizzlyHttpServerFactory
                    .createHttpServer(BASE_URI, new EntityFilteringApplication(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println("Application started.\nTry out one of these URIs:");
            for (final String path : new String[]{"projects", "projects/detailed", "users", "users?detailed=true", "tasks",
                    "tasks/detailed"}) {
                System.out.println(BASE_URI + path);
            }
            System.out.println("Stop the application using CTRL+C");

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName())
                    .log(Level.SEVERE, "I/O error occurred during reading from an system input stream.", ex);
        }
    }
}
