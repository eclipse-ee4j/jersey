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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GZIPContentEncodingTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(GZIPContentEncodingTest.class.getName());

    @Path("/")
    public static class Resource {

        @POST
        public byte[] post(byte[] content) {
            return content;
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(Resource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(GZipEncoder.class);
        config.connectorProvider(new JavaNetHttpConnectorProvider());
    }

    @Test
    public void testPost() {
        WebTarget r = target();
        byte[] content = new byte[1024 * 1024];
        content[0] = 1;
        assertTrue(Arrays.equals(content,
                r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE)).readEntity(byte[].class)));

        Response cr = r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPostChunked() {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
        config.connectorProvider(new JavaNetHttpConnectorProvider());
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));

        Client client = ClientBuilder.newClient(config);
        WebTarget r = client.target(getBaseUri());

        byte[] content = new byte[1024 * 1024];
        assertTrue(Arrays.equals(content,
                r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE)).readEntity(byte[].class)));

        Response cr = r.request().post(Entity.text("POST"));
        assertTrue(cr.hasEntity());
        cr.close();

        client.close();
    }

}