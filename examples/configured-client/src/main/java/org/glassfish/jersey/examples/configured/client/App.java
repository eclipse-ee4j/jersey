/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.configured.client;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Hello world!
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");
    public static final String ROOT_PATH = "helloworld";
    /* package */ static final String ENTITY_PROPERTY = "entity.value";

    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey Example App");

            final ResourceConfig resourceConfig = new ResourceConfig(HelloWorldResource.class);
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            WebTarget target = ClientBuilder.newClient().target(BASE_URI);
            Object entity = target.getConfiguration().getProperty(ENTITY_PROPERTY);
            Object provider = target.getConfiguration().getProperty(ClientProperties.CONNECTOR_PROVIDER);

            System.out
                    .append("  Application started.\n")
                    .append("  Sending entity \"").append((String) entity).append("\"")
                    .append(" using ").append((String) provider).append(" connector provider")
                    .append(" to echo resource ").append(BASE_URI.toASCIIString()).println(ROOT_PATH);

            try (Response response = target.path(ROOT_PATH).request().post(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE))) {
                System.out.append("  Recieved: \"").append(response.readEntity(String.class)).println("\"");
            }

            server.stop();
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
