/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.osgi.test.basic;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;

/**
 * @author Jakub Podlesak
 * @author Michal Gajdos
 */
@RunWith(PaxExam.class)
public class BasicOsgiIntegrationTest {

    private static final String CONTEXT = "/jersey";

    private static final URI baseUri = UriBuilder
            .fromUri("http://localhost")
            .port(Helper.getPort())
            .path(CONTEXT).build();

    @Configuration
    public static Option[] configuration() {
        List<Option> options = Helper.getCommonOsgiOptions();
        options.add(frameworkProperty("felix.log.level").value("4"));
        return Helper.asArray(options);
    }

    @Path("/super-simple")
    public static class SuperSimpleResource {

        @GET
        public String getMe() {
            return "OK";
        }
    }

    @Test
    public void testSimpleResource() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(SuperSimpleResource.class);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

        Client c = ClientBuilder.newClient();
        final Response response = c.target(baseUri).path("/super-simple").request().buildGet().invoke();

        String result = response.readEntity(String.class);
        System.out.println("RESULT = " + result);

        assertEquals("OK", result);

        server.shutdownNow();
    }
}
