/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Martin Matula
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class TimeoutTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(TimeoutTest.class.getName());

    @Path("/test")
    public static class TimeoutResource {
        @GET
        public String get() {
            return "GET";
        }

        @GET
        @Path("timeout")
        public String getTimeout() {
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            return "GET";
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig config = new ResourceConfig(TimeoutResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        return config;
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.property(ClientProperties.READ_TIMEOUT, 1000);
        config.connectorProvider(new ApacheConnectorProvider());
    }

    @Test
    public void testFast() {
        final Response r = target("test").request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
    }

    @Test
    public void testSlow() {
        try {
            target("test/timeout").request().get();
            fail("Timeout expected.");
        } catch (final ProcessingException e) {
            assertThat("Unexpected processing exception cause",
                    e.getCause(), instanceOf(SocketTimeoutException.class));
        }
    }

    @Test
    public void testPerRequestTimeout() {
        final Response r = target("test/timeout").request()
                .property(ClientProperties.READ_TIMEOUT, 3000).get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
    }
}
