#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package ${package};

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * @author ${projectAuthor}
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/base/");

    public static final String ROOT_PATH = "resource-path";

    public static void main(String[] args) {
        try {
            System.out.println("${projectName}");

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new MyApplication(), false);
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
            server.start();

            System.out.println(String.format(
                    "Application started.${symbol_escape}n"
                            + "Try out %s%s${symbol_escape}n"
                            + "Stop the application using CTRL+C",
                    BASE_URI, ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
