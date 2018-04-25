/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;

/**
 * Jersey example application for custom executors managed async resources.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    public static final String ASYNC_MESSAGING_FIRE_N_FORGET_PATH = "async/messaging/fireAndForget";
    public static final String ASYNC_MESSAGING_BLOCKING_PATH = "async/messaging/blocking";
    public static final String ASYNC_LONG_RUNNING_OP_PATH = "async/longrunning";

    public static void main(String[] args) {
        try {
            System.out.println("\"Async resources\" Jersey Example App");

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
                            + "To test simple, non-blocking asynchronous messaging resource, try %s%s\n"
                            + "To test blocking version of asynchronous messaging resource, try %s%s\n"
                            + "To test long-running asynchronous operation resource, try %s%s\n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, ASYNC_MESSAGING_FIRE_N_FORGET_PATH,
                    BASE_URI, ASYNC_MESSAGING_BLOCKING_PATH,
                    BASE_URI, ASYNC_LONG_RUNNING_OP_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static ResourceConfig create() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .registerClasses(BlockingPostChatResource.class, FireAndForgetChatResource.class, SimpleLongRunningResource.class)
                .registerInstances(new LoggingFeature(Logger.getLogger(App.class.getName()), PAYLOAD_ANY));

        return resourceConfig;
    }
}
