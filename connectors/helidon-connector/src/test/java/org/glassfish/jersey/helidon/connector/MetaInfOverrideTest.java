/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

// The Helidon jar has META-INF set
// Test of override
class MetaInfOverrideTest extends JerseyTest {

    @Path("/origin")
    public static class UserAgentServer {
        @GET
        public String get(@Context HttpHeaders headers) {
            return headers.getHeaderString(HttpHeaders.USER_AGENT);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(UserAgentServer.class);
    }

    @Test
    void defaultMetaInfTest() {
        try (Response r = target("origin").request().get()) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertTrue(r.readEntity(String.class).contains("Helidon"));
        }
    }

    @Test
    void overrideMetaInfTest() {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new HttpUrlConnectorProvider());
        try (Response r = ClientBuilder.newClient(config).target(target("origin").getUri()).request().get()) {
            Assertions.assertEquals(200, r.getStatus());
            r.bufferEntity();
            System.out.println(r.readEntity(String.class));
            Assertions.assertTrue(r.readEntity(String.class).contains("HttpUrlConnection"));
        }
    }

    @Test
    void overrideMetaInfByOtherConfigPropertyTest() {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECTOR_PROVIDER, "org.glassfish.jersey.client.HttpUrlConnectorProvider");
        try (Response r = ClientBuilder.newClient(config).target(target("origin").getUri()).request().get()) {
            Assertions.assertEquals(200, r.getStatus());
            r.bufferEntity();
            System.out.println(r.readEntity(String.class));
            Assertions.assertTrue(r.readEntity(String.class).contains("HttpUrlConnection"));
        }
    }

    @Test
    void overrideMetaInfByThePropertyTest() {
        try (Response r = ClientBuilder.newBuilder()
                .property(ClientProperties.CONNECTOR_PROVIDER, "org.glassfish.jersey.client.HttpUrlConnectorProvider")
                .build()
                .target(target("origin").getUri()).request().get()) {
            Assertions.assertEquals(200, r.getStatus());
            r.bufferEntity();
            System.out.println(r.readEntity(String.class));
            Assertions.assertTrue(r.readEntity(String.class).contains("HttpUrlConnection"));
        }
    }
}
