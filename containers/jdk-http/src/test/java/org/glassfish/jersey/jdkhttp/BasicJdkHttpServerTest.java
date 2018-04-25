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

package org.glassfish.jersey.jdkhttp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.After;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

/**
 * Jdk Http Server basic tests.
 *
 * @author Michal Gajdos
 */
public class BasicJdkHttpServerTest extends AbstractJdkHttpServerTester {

    private HttpServer server;

    @Path("/test")
    public static class TestResource {

        @GET
        public String get() {
            return "test";
        }
    }

    @Test
    public void testCreateHttpServer() throws Exception {
        server = JdkHttpServerFactory.createHttpServer(
                UriBuilder.fromUri("http://localhost/").port(getPort()).build(), new ResourceConfig(TestResource.class));

        assertThat(server, instanceOf(HttpServer.class));
        assertThat(server, not(instanceOf(HttpsServer.class)));
    }

    @Test
    public void testCreateHttpsServer() throws Exception {
        server = JdkHttpServerFactory.createHttpServer(
                UriBuilder.fromUri("https://localhost/").port(getPort()).build(),
                new ResourceConfig(TestResource.class),
                false);

        assertThat(server, instanceOf(HttpsServer.class));
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(3);
            server = null;
        }
    }
}
