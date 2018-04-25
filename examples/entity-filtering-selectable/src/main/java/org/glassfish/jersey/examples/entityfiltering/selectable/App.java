/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.selectable;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

/**
 * Java application class starting Grizzly2 server with Entity Data Filtering with query parameters.
 *
 * @author Andy Pemberton (pembertona at oracle.com)
 */
public final class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    public static void main(final String[] args) {
        try {
            System.out.println("Jersey Entity Data Filtering Example.");

            final HttpServer server = GrizzlyHttpServerFactory
                    .createHttpServer(BASE_URI, new SelectableEntityFilteringApplication(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println("Application started.\nTry out one of these URIs:");
            for (final String path : new String[]{"people/1234", "people/1234?select=familyName,givenName",
                    "people/1234?select=region,addresses.region",
                    "people/1234?select=familyName,givenName,addresses.phoneNumber.number"}) {
                System.out.println(BASE_URI + path);
            }
            System.out.println("Stop the application using CTRL+C");

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Prevent instantiation.
     */
    private App() {
    }
}
