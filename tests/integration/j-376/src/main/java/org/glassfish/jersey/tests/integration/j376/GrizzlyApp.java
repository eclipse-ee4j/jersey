/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j376;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.environment.se.Weld;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI Test App launcher. Starts the Grizzly server and initializes weld.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class GrizzlyApp {

    private static Weld weld;
    private static HttpServer server;

    private static final URI BASE_URI = URI.create("http://localhost:8080/j376/");

    public static void main(String[] args) {
        try {
            System.out.println("Jersey CDI Test App");

            start();

            System.out.println(String.format("Application started.\nTry out %s%s\nHit enter to stop it...",
                    BASE_URI, "application.wadl"));
            System.in.read();
            stop();
        } catch (IOException ex) {
            Logger.getLogger(GrizzlyApp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected static void stop() {
        server.shutdownNow();
        weld.shutdown();
    }

    protected static void start() {
        weld = new Weld();
        weld.initialize();

        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, createJaxRsApp(), true);
    }

    public static URI getBaseUri() {
        return BASE_URI;
    }

    public static ResourceConfig createJaxRsApp() {
        return new ResourceConfig(new MyApplication().getClasses());
    }
}
