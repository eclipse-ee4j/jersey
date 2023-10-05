/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.examples.micrometer;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.micrometer.server.DefaultJerseyTagsProvider;
import org.glassfish.jersey.micrometer.server.MetricsApplicationEventListener;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/micro/");
    public static final String ROOT_PATH = "meter";

    public static void main(String[] args) {
        try {
            System.out.println("Micrometer/ Jersey Basic Example App");

            final MeterRegistry registry = new SimpleMeterRegistry();

            final ResourceConfig resourceConfig = new ResourceConfig(MicrometerResource.class)
                    .register(new MetricsApplicationEventListener(registry, new DefaultJerseyTagsProvider(),
                    "http.shared.metrics", true))
                    .register(new MetricsResource(registry));
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out                        %s%s\n"
                            + "After several requests go to   %s%s\nAnd after that go to the       %s%s\n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH, BASE_URI, "metrics", BASE_URI, "metrics/metrics"));
            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
