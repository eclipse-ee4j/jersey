/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import jakarta.inject.Inject;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the presence of the first byte in very large stream in each of 1000 requests
 * Particularly for {@link org.glassfish.jersey.message.internal.EntityInputStream#isEmpty} method
 * <p>
 * Also introduces mixture of the Jetty server and servlet holder in which the error was reproduced.
 */
public class ResponseReadEntityStreamTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return (baseUri, deploymentContext) -> {
            final Server server = JettyHttpContainerFactory.createServer(baseUri, false);
            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(9001);
            server.addConnector(connector);

            final ResourceConfig resConfig = new ResourceConfig(Analyze.class);

            final ServletContainer jerseyServletContainer = new ServletContainer(resConfig);
            final ServletHolder jettyServletHolder = new ServletHolder(jerseyServletContainer);

            final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");

            context.addServlet(jettyServletHolder, "/api/*");

            server.setHandler(context);
            return new TestContainer() {
                @Override
                public ClientConfig getClientConfig() {
                    return new ClientConfig();
                }

                @Override
                public URI getBaseUri() {
                    return baseUri;
                }

                @Override
                public void start() {
                    try {
                        server.start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void stop() {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        };
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ResponseReadEntityStreamTest.Analyze.class);
    }

    private static void generateJson(final String filePath, int recordCount) throws IOException {

        try (final JsonGenerator generator = new JsonFactory().createGenerator(new FileWriter(filePath))) {
            generator.writeStartArray();

            for (int i = 0; i < recordCount; i++) {
                generator.writeStartObject();
                generator.writeNumberField("id", i);
                generator.writeStringField("name", "User" + i);
                // Add more fields as needed
                generator.writeEndObject();

                if (i % 10000 == 0) {
                    generator.flush();
                }
            }

            generator.writeEndArray();
        }
    }

    @Test
    public void readEntityTest() throws IOException {
        final Invocation.Builder requestBuilder = target("/api/v1/analyze").request();
        //iterate 1000 requests to be sure the first byte is not lost
        final String fileName = "bigFile.json";
        final String path = "target/" + fileName;

        final java.nio.file.Path pathResource = Paths.get(path);

        final java.nio.file.Path realFilePath = Files.createFile(pathResource.toAbsolutePath());

        try {

            generateJson(realFilePath.toString(), 1000000); // 33Mb real file size

            final File bigFile = realFilePath.toFile();

            for (int i = 1; i < 1000; i++) {
                try (final FileInputStream stream = new FileInputStream(bigFile)) {
                    final Response response = requestBuilder.post(Entity.entity(stream,
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE));
                    assertEquals(200, response.getStatus());
                    response.close();
                }
            }
        } finally {
            Files.deleteIfExists(pathResource);
        }

    }

    @Path("/v1")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Analyze {

        @Inject
        private HttpServletRequest request;

        @POST
        @Path("/analyze")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response analyze() {

            try (final ServletInputStream inputStream = request.getInputStream()) {
                final byte[] content = inputStream.readAllBytes();

                if (content[0] != 91 /* character [ */) { // https://www.ascii-code.com/91
                    throw new Exception("Oops");
                }

                return Response.ok("{\"status\":\"OK\"}").build();

            } catch (Exception e) {
                return Response.serverError().build();
            }

        }
    }

}
