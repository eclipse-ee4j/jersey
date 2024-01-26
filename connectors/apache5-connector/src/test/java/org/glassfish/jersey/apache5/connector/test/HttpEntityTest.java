/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

public class HttpEntityTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(HttpEntityTest.class.getName());
    private static final String ECHO_MESSAGE = "ECHO MESSAGE";

    @Path("/")
    public static class Resource {
        @POST
        public String echo(String message) {
            return message;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        config.connectorProvider(new Apache5ConnectorProvider());
    }

    @Test
    public void testInputStreamEntity() {
        ByteArrayInputStream bais = new ByteArrayInputStream(ECHO_MESSAGE.getBytes());
        InputStreamEntity entity = new InputStreamEntity(bais, ContentType.TEXT_PLAIN);

        try (Response response = target().request().post(Entity.entity(entity, MediaType.APPLICATION_OCTET_STREAM))) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals(ECHO_MESSAGE, response.readEntity(String.class));
        }
    }

    @Test
    public void testByteArrayEntity() {
        ByteArrayEntity entity = new ByteArrayEntity(ECHO_MESSAGE.getBytes(), ContentType.TEXT_PLAIN);

        try (Response response = target().request().post(Entity.entity(entity, MediaType.APPLICATION_OCTET_STREAM))) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals(ECHO_MESSAGE, response.readEntity(String.class));
        }
    }
}
