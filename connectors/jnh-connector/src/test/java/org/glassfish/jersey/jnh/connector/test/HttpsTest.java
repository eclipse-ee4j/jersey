/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import java.net.URI;
import java.util.Optional;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpsTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(HttpsTest.class.getName());
    private static final String ROOT_PATH = "test";

    @Path(ROOT_PATH)
    public static class Resource {
        @GET
        public String get() {
            return "GET";
        }
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri("https://localhost").port(getPort()).build();
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(Resource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected Optional<SSLContext> getSslContext() {
        return Optional.of(SslUtils.createServerSslContext(true, true));
    }

    @Override
    protected Optional<SSLParameters> getSslParameters() {
        SSLParameters serverSslParameters = new SSLParameters();
        serverSslParameters.setNeedClientAuth(true);
        return Optional.of(serverSslParameters);
    }

    private SSLContext clientSslContext() {
        return SslUtils.createClientSslContext(true, true);
    }

    @Test
    public void testConnection() {
        ClientConfig cc = new ClientConfig()
                .connectorProvider(new JavaNetHttpConnectorProvider());
        Client client = ClientBuilder.newBuilder()
            .withConfig(cc)
            .sslContext(clientSslContext())
            .register(LoggingFeature.class)
            .build();
        Response response = client.target(getBaseUri()).path(ROOT_PATH).request().get();
        assertEquals(200, response.getStatus());
        assertEquals("GET", response.readEntity(String.class));
    }
}
