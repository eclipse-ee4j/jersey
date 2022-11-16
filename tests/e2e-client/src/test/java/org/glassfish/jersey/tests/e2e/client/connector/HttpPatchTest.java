/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Pavel Bucek
 */
public class HttpPatchTest {

    private static final Logger LOGGER = Logger.getLogger(RequestHeaderModificationsTest.class.getName());

    public static List<ConnectorProvider> testData() {
        return Arrays.asList(
                // {HttpUrlConnectorProvider.class}, // cannot process PATCH without additional configuration
                new GrizzlyConnectorProvider(),
                new JettyConnectorProvider(), // unstable.
                new ApacheConnectorProvider(),
                new Apache5ConnectorProvider(),
                new NettyConnectorProvider(),
                new JdkConnectorProvider()
                );
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        for (ConnectorProvider provider : testData()) {
            HttpPatchTemplateTest test = new HttpPatchTemplateTest(provider) {};
            DynamicContainer container = TestHelper.toTestContainer(test,
                    String.format("httpPatchTest (%s)", provider.getClass().getSimpleName()));
            tests.add(container);
        }
        return tests;
    }

    public abstract static class HttpPatchTemplateTest extends JerseyTest {
        private final ConnectorProvider connectorProvider;

        public HttpPatchTemplateTest(ConnectorProvider connectorProvider) {
            this.connectorProvider = connectorProvider;
        }

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());
            enable(TestProperties.LOG_TRAFFIC);
            return new ResourceConfig(PatchResource.class)
                    .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.HEADERS_ONLY));
        }

        @Override
        protected void configureClient(ClientConfig clientConfig) {
            clientConfig.connectorProvider(connectorProvider);
        }

        @Test
        public void testPatchResponse() throws Exception {
            Response response = target().request().method("PATCH", Entity.text("patch"));

            assertEquals(200, response.getStatus());
            assertEquals("patch", response.readEntity(String.class));
        }

        @Test
        public void testPatchEntity() throws Exception {
            String response = target().request().method("PATCH", Entity.text("patch"), String.class);

            assertEquals("patch", response);
        }

        @Test
        public void testPatchGenericType() throws Exception {
            String response = target().request().method("PATCH", Entity.text("patch"), new GenericType<String>() {
            });

            assertEquals("patch", response);
        }

        @Test
        public void testAsyncPatchResponse() throws Exception {
            Future<Response> response = target().request().async().method("PATCH", Entity.text("patch"));

            assertEquals(200, response.get().getStatus());
            assertEquals("patch", response.get().readEntity(String.class));
        }

        @Test
        public void testAsyncPatchEntity() throws Exception {
            Future<String> response = target().request().async().method("PATCH", Entity.text("patch"), String.class);

            assertEquals("patch", response.get());
        }

        @Test
        public void testAsyncPatchGenericType() throws Exception {
            Future<String> response = target().request().async().method("PATCH", Entity.text("patch"), new GenericType<String>() {
            });

            assertEquals("patch", response.get());
        }

        @Test
        public void testRxPatchResponse() throws Exception {
            CompletionStage<Response> response = target().request().rx().method("PATCH", Entity.text("patch"));

            assertEquals(200, response.toCompletableFuture().get().getStatus());
            assertEquals("patch", response.toCompletableFuture().get().readEntity(String.class));
        }

        @Test
        public void testRxPatchEntity() throws Exception {
            CompletionStage<String> response = target().request().rx().method("PATCH", Entity.text("patch"), String.class);

            assertEquals("patch", response.toCompletableFuture().get());
        }

        @Test
        public void testRxPatchGenericType() throws Exception {
            CompletionStage<String> response = target().request().rx()
                                                       .method("PATCH", Entity.text("patch"), new GenericType<String>() {
                                                       });

            assertEquals("patch", response.toCompletableFuture().get());
        }
    }

    @Path("/")
    public static class PatchResource {

        @PATCH
        public String patch(String entity) {

            System.out.println("SERVER: patch request received.");

            return entity;
        }
    }
}
