/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Default synchronous jetty client implementation has a hard response size limit of 2MiB.
 * When response is too big, a processing exception is thrown.
 * The original code path was left to preserve this behaviour but could be removed
 * and reworked in the future with a custom listener like async path.
 *
 * This tests the previous behavior with large payloads (>2MiB), the new size override (4MiB)
 * and very big payloads (>4MiB).
 *
 * @author cen1 (cen.is.imba at gmail.com)
 */
public class SyncResponseSizeTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(SyncResponseSizeTest.class.getName());

    private static final int maxBufferSize = 4 * 1024 * 1024; //4 MiB

    @Path("/test")
    public static class TimeoutResource {

        private static final byte[] data = new byte[maxBufferSize];

        static {
            Byte b = "a".getBytes()[0];
            for (int i = 0; i < maxBufferSize; i++) data[i] = b.byteValue();
        }

        @GET
        @Path("/small")
        public String getSmall() {
            return "GET";
        }

        @GET
        @Path("/big")
        public String getBig() {
            return new String(data);
        }

        @GET
        @Path("/verybig")
        public String getVeryBig() {
            return new String(data) + "a";
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(TimeoutResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void testDefaultSmall() {
        Response r = target("test/small").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
    }

    @Test
    public void testDefaultTooBig() {
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.READ_TIMEOUT, 1_000);
        config.connectorProvider(new JettyConnectorProvider());

        Client c = ClientBuilder.newClient(config);
        WebTarget t = c.target(u);
        try {
            t.path("test/big").request().get();
            fail("Exception expected.");
        } catch (ProcessingException e) {
            // Buffering capacity ... exceeded.
            assertTrue(ExecutionException.class.isInstance(e.getCause()));
            assertTrue(IllegalArgumentException.class.isInstance(e.getCause().getCause()));
        } finally {
            c.close();
        }
    }

    @Test
    public void testCustomBig() {
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.READ_TIMEOUT, 1_000);
        config.connectorProvider(new JettyConnectorProvider());
        config.property(JettyClientProperties.SYNC_LISTENER_RESPONSE_MAX_SIZE, maxBufferSize);

        Client c = ClientBuilder.newClient(config);
        WebTarget t = c.target(u);
        try {
            Response r = t.path("test/big").request().get();
            String p = r.readEntity(String.class);
            assertEquals(p.length(), maxBufferSize);
        } catch (ProcessingException e) {
            assertThat("Unexpected processing exception cause",
                e.getCause(), instanceOf(TimeoutException.class));
        } finally {
            c.close();
        }
    }

    @Test
    public void testCustomTooBig() {
        final URI u = target().getUri();
        ClientConfig config = new ClientConfig().property(ClientProperties.READ_TIMEOUT, 1_000);
        config.connectorProvider(new JettyConnectorProvider());
        config.property(JettyClientProperties.SYNC_LISTENER_RESPONSE_MAX_SIZE, maxBufferSize);

        Client c = ClientBuilder.newClient(config);
        WebTarget t = c.target(u);
        try {
            t.path("test/verybig").request().get();
            fail("Exception expected.");
        } catch (ProcessingException e) {
            // Buffering capacity ... exceeded.
            assertTrue(ExecutionException.class.isInstance(e.getCause()));
            assertTrue(IllegalArgumentException.class.isInstance(e.getCause().getCause()));
        } finally {
            c.close();
        }
    }
}
