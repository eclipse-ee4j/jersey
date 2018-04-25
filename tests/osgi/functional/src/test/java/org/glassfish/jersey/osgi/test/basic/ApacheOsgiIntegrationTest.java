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

package org.glassfish.jersey.osgi.test.basic;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@RunWith(PaxExam.class)
public class ApacheOsgiIntegrationTest {

    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(Helper.getPort()).path("/jersey").build();

    @Configuration
    public static Option[] configuration() {
        final List<Option> options = Helper.getCommonOsgiOptions();
        options.addAll(Helper.expandedList(
                mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-api").versionAsInProject(),
                mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject(),
                mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject(),
                mavenBundle().groupId("org.glassfish.jersey.connectors").artifactId("jersey-apache-connector")
                        .versionAsInProject()

        ));
        return Helper.asArray(options);
    }

    @Path("/apacheOsgiTest")
    public static class ApacheOsgiTestResource {

        @GET
        public String getMe() {
            return "OK";
        }
    }

    @Test
    public void testSimpleResource() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(ApacheOsgiTestResource.class);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        final Client c = ClientBuilder.newClient(clientConfig);

        final Response response = c.target(baseUri).path("/apacheOsgiTest").request().buildGet().invoke();

        final String result = response.readEntity(String.class);
        assertEquals("OK", result);
        server.shutdownNow();
    }
}
