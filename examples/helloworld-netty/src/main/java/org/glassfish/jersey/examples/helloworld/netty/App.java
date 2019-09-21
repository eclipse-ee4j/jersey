/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.netty;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Hello world!
 */
public class App {

    static final String ROOT_PATH = "helloworld";

    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey Example App on Netty container.");

            ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class);
            final Channel server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.close();
                }
            }));

            System.out.println(String.format("Application started. (HTTP/2 enabled!)\nTry out %s%s\nStop the application using "
                                                     + "CTRL+C.", BASE_URI, ROOT_PATH));
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
