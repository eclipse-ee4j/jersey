/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sysprops;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.examples.sysprops.impl.PropertyNamesResourceImpl;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * @author Martin Matula
 */
public class App {
    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    public static void main(String[] args) {
        try {
            System.out.println("System Properties Jersey Example App");

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
                            + "Try out %s%n"
                            + "Stop the application using CTRL+C",
                            BASE_URI + "/properties"));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static ResourceConfig createApp() {
        return new ResourceConfig(
                PropertyNamesResourceImpl.class,
                PropertiesWriter.class
        );
    }
}
