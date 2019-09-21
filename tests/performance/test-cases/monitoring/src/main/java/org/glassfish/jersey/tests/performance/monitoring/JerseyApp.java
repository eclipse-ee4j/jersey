/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.monitoring;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.*;
import org.glassfish.jersey.server.model.Resource;

/**
 * Application class to start performance test web service at http://localhost:8080/ if the base URI
 * is not passed via the first command line argument.
 */
public class JerseyApp {

    private static final URI BASE_URI = URI.create("http://localhost:8080/");
    public static final String ROOT_PATH = "text";

    public static void main(final String[] args) throws Exception {
            System.out.println("Jersey performance test web service application");

            final URI baseUri = args.length > 0 ? URI.create(args[0]) : BASE_URI;
            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, createResourceConfig());

            System.out.println(String.format("Application started.\nTry out %s%s\nHit Ctrl-C to stop it...",
                    baseUri, ROOT_PATH));

            while (server.isStarted()) {
                Thread.sleep(600000);
            }
    }

    private static ResourceConfig createResourceConfig() {
        final Set<Resource> resources = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            resources.add(Resource.builder(org.glassfish.jersey.tests.performance.monitoring.Resource.class)
                    .path("" + i)
                    .build());
        }

        return new ResourceConfig()
                .property(ServerProperties.MONITORING_STATISTICS_ENABLED, true)
                .property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true)
                .registerResources(resources);
    }
}
