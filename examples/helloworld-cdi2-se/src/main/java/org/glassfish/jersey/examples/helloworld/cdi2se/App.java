/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Hello world!
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    public static final String ROOT_HELLO_PATH = "helloworld";
    public static final String ROOT_COUNTER_PATH = "counter";

    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey Example App");

            ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class, CounterResource.class);
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

            server.start();

            System.out.println("Application started.\nTry out");
            System.out.println(String.format("%s%s", BASE_URI, ROOT_HELLO_PATH));
            System.out.println(String.format("%s%s%s", BASE_URI, ROOT_COUNTER_PATH, "/request"));
            System.out.println(String.format("%s%s%s", BASE_URI, ROOT_COUNTER_PATH, "/application"));
            System.out.println("Stop the application using CTRL+C");

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
