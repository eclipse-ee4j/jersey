/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.exception;

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
    public static final String ROOT_PATH = "exception";

    public static void main(String[] args) {
        try {
            System.out.println("\"Exception Mapping\" Jersey Example App");

            final ResourceConfig resourceConfig = new ResourceConfig(
                    ExceptionResource.class,
                    ExceptionResource.MyResponseFilter.class,
                    ExceptionResource.WebApplicationExceptionFilter.class,
                    Exceptions.MyExceptionMapper.class,
                    Exceptions.MySubExceptionMapper.class,
                    Exceptions.WebApplicationExceptionMapper.class);

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format(
                    "Application started.%n"
                    + "Try out %s%s%n"
                    + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
