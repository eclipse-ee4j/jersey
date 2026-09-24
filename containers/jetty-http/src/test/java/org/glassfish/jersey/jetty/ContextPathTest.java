/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability under the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.jetty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextPathTest {

    @Test
    public void testContainerMountedAtRootContextPath() throws Exception {
        testContainer("/", "/resource?value=query", "/\n/resource\nquery");
    }

    @Test
    public void testContainerMountedAtNonRootContextPath() throws Exception {
        testContainer("/context", "/context/resource?value=query",
                "/context/\n/context/resource\nquery");
    }

    private void testContainer(String contextPath, String requestTarget, String expectedContent) throws Exception {
        final Server server = new Server();
        final LocalConnector connector = new LocalConnector(server);
        server.addConnector(connector);
        final JettyHttpContainer container = ContainerFactory.createContainer(
                JettyHttpContainer.class, new ResourceConfig(ContextPathResource.class));
        server.setHandler(new ContextHandler(container, contextPath));
        server.start();

        try {
            final HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(
                    "GET " + requestTarget + " HTTP/1.1\r\n"
                            + "Host: localhost\r\n"
                            + "Connection: close\r\n\r\n"));

            assertEquals(200, response.getStatus());
            assertEquals(expectedContent, response.getContent());
        } finally {
            server.stop();
        }
    }

    @Path("resource")
    public static class ContextPathResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get(@Context UriInfo uriInfo, @QueryParam("value") String value) {
            return uriInfo.getBaseUri().getPath()
                    + "\n" + uriInfo.getRequestUri().getPath()
                    + "\n" + value;
        }
    }
}
