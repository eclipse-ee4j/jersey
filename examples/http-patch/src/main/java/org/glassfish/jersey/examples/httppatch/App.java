/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httppatch;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * HTTP PATCH Demo Application.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class App {
    /**
     * Prevents instantiation.
     */
    private App() {
    }

    private static final URI BASE_URI = URI.create("http://localhost:8080/http-patch");
    /**
     * Root resource path.
     */
    static final String ROOT_PATH = "patchable-state";

    public static void main(String[] args) {
        try {
            System.out.println("Jersey HTTP PATCH Support Example App");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, create(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(String.format("Application started.\nTry out %s/%s\nStop the application using CTRL+C",
                    BASE_URI,
                    ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create JAX-RS application for the example.
     *
     * @return created application instance.
     */
    public static ResourceConfig create() {
        final ResourceConfig app = new ResourceConfig(
                PatchableResource.class,
                OptionsAcceptPatchHeaderFilter.class,
                PatchingInterceptor.class);

        app.register(createMoxyJsonResolver());

        // Enable on-demand tracing
        app.property(ServerProperties.TRACING, "ON_DEMAND");

        return app;
    }

    /**
     * Create {@link javax.ws.rs.ext.ContextResolver} for {@link org.glassfish.jersey.moxy.json.MoxyJsonConfig}
     * for this application.
     *
     * @return {@code MoxyJsonConfig} context resolver.
     */
    public static ContextResolver<MoxyJsonConfig> createMoxyJsonResolver() {
        final MoxyJsonConfig moxyJsonConfig = new MoxyJsonConfig()
                .setFormattedOutput(true)
                .setNamespaceSeparator(':');
        return moxyJsonConfig.resolver();
    }
}
