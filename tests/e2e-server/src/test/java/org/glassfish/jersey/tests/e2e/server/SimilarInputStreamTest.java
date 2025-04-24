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

package org.glassfish.jersey.tests.e2e.server;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimilarInputStreamTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return (baseUri, deploymentContext) -> {
            final Server server = JettyHttpContainerFactory.createServer(baseUri, false);
            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(9001);
            server.addConnector(connector);

            final ServletContainer jerseyServletContainer = new ServletContainer(deploymentContext.getResourceConfig());
            final ServletHolder jettyServletHolder = new ServletHolder(jerseyServletContainer);

            final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");

            // filter which will change the http servlet request to have a reply-able input stream
            context.addFilter(FilterSettingMultiReadRequest.class,
                    "/*", EnumSet.allOf(DispatcherType.class));
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
        ResourceConfig resourceConfig = new ResourceConfig(TestResource.class);
        // force jersey to use jackson for deserialization
        resourceConfig.addProperties(
                Collections.singletonMap(InternalProperties.JSON_FEATURE, JacksonFeature.class.getSimpleName()));
        return resourceConfig;
    }

    @Test
    public void readJsonWithReplayableInputStreamFailsTest() {
        final Invocation.Builder requestBuilder = target("/api/v1/echo").request();
        final MyDto myDto = new MyDto();
        myDto.setMyField("Something");
        try (Response response = requestBuilder.post(Entity.entity(myDto, MediaType.APPLICATION_JSON))) {
            // fixed from failure with a 400 as jackson can never finish reading the input stream
            assertEquals(200, response.getStatus());
            final MyDto resultDto = response.readEntity(MyDto.class);
            assertEquals("Something", resultDto.getMyField()); //verify we still get Something
        }
    }

    @Path("/v1")
    public static class TestResource {

        @POST
        @Path("/echo")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        public MyDto echo(MyDto input) {
            return input;
        }
    }

    public static class MyDto {
        private String myField;

        public String getMyField() {
            return myField;
        }

        public void setMyField(String myField) {
            this.myField = myField;
        }

        @Override
        public String toString() {
            return "MyDto{"
                    + "myField='" + myField + '\''
                    + '}';
        }
    }


    public static class FilterSettingMultiReadRequest implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            /* wrap the request in order to read the inputstream multiple times */
            MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);
            chain.doFilter(multiReadRequest, response);
        }
    }

    static class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
        private byte[] cachedBytes;

        public MultiReadHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (cachedBytes == null) {
                cacheInputStream();
            }

            return new CachedServletInputStream(cachedBytes);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        private void cacheInputStream() throws IOException {
            // Cache the inputstream in order to read it multiple times.
            cachedBytes = IOUtils.toByteArray(super.getInputStream());
        }


        /* An input stream which reads the cached request body */
        private class CachedServletInputStream extends ServletInputStream {

            private final ByteArrayInputStream buffer;

            public CachedServletInputStream(byte[] contents) {
                this.buffer = new ByteArrayInputStream(contents);
            }

            @Override
            public int read() {
                return buffer.read();
            }

            @Override
            public boolean isFinished() {
                return buffer.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new RuntimeException("Not implemented");
            }
        }
    }
}
