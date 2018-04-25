/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async.managed;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

/**
 * Jersey example application for custom executors managed async resources.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    public static final String ASYNC_LONG_RUNNING_MANAGED_OP_PATH = "managedasync/longrunning";

    public static void main(String[] args) {
        try {
            System.out.println("\"Custom Executor Managed Async Resources\" Jersey Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\n"
                    + "To test long-running asynchronous operation resource, try %s%s\n"
                    + "To test async chat resource, try %s%s\n"
                    + "Stop the application using CTRL+C", BASE_URI, ASYNC_LONG_RUNNING_MANAGED_OP_PATH, BASE_URI, "chat"));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ResourceConfig create() {
        return new ResourceConfig()
                .registerClasses(JacksonFeature.class, ChatResource.class, SimpleJerseyExecutorManagedLongRunningResource.class)
                .registerInstances(new LoggingFeature(Logger.getLogger(App.class.getName()), PAYLOAD_ANY));
    }
}
