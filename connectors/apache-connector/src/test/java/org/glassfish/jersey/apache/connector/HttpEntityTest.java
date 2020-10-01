/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache.connector;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        config.connectorProvider(new ApacheConnectorProvider());
    }

    @Test
    public void testInputStreamEntity() {
        ByteArrayInputStream bais = new ByteArrayInputStream(ECHO_MESSAGE.getBytes());
        InputStreamEntity entity = new InputStreamEntity(bais);

        try (Response response = target().request().post(Entity.entity(entity, MediaType.APPLICATION_OCTET_STREAM))) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(ECHO_MESSAGE, response.readEntity(String.class));
        }
    }

    @Test
    public void testByteArrayEntity() {
        ByteArrayEntity entity = new ByteArrayEntity(ECHO_MESSAGE.getBytes());

        try (Response response = target().request().post(Entity.entity(entity, MediaType.APPLICATION_OCTET_STREAM))) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(ECHO_MESSAGE, response.readEntity(String.class));
        }
    }
}
