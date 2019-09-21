/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi;

import java.io.IOException;

import java.net.URI;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.weld.environment.se.Weld;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.examples.cdi.resources.MyApplication;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Hello world!
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/cdi-webapp/");

    public static void main(String[] args) {
        try {
            System.out.println("Jersey CDI Example App");

            final Weld weld = new Weld();
            weld.initialize();

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, createJaxRsApp(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                    weld.shutdown();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C",
                    BASE_URI, "application.wadl"));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static ResourceConfig createJaxRsApp() {
        return new ResourceConfig(new MyApplication().getClasses());
    }
}
