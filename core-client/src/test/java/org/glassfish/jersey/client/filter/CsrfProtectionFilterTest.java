/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.filter;

import java.util.concurrent.Future;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Cross-site request forgery client filter test.
 *
 * @author Martin Matula
 */
public class CsrfProtectionFilterTest {
    private Invocation.Builder invBuilder;

    @Before
    public void setUp() {
        Client client = ClientBuilder.newClient(new ClientConfig(CsrfProtectionFilter.class)
                .connectorProvider(new TestConnector()));
        invBuilder = client.target(UriBuilder.fromUri("/").build()).request();
    }

    @Test
    public void testGet() {
        Response r = invBuilder.get();
        assertNull(r.getHeaderString(CsrfProtectionFilter.HEADER_NAME));
    }

    @Test
    public void testPut() {
        Response r = invBuilder.put(Entity.entity("put", MediaType.TEXT_PLAIN_TYPE));
        assertNotNull(r.getHeaderString(CsrfProtectionFilter.HEADER_NAME));
    }

    private static class TestConnector implements Connector, ConnectorProvider {

        @Override
        public Connector getConnector(Client client, Configuration runtimeConfig) {
            return this;
        }

        @Override
        public ClientResponse apply(ClientRequest requestContext) {
            final ClientResponse responseContext = new ClientResponse(
                    Response.Status.OK, requestContext);

            final String headerValue = requestContext.getHeaderString(CsrfProtectionFilter.HEADER_NAME);
            if (headerValue != null) {
                responseContext.header(CsrfProtectionFilter.HEADER_NAME, headerValue);
            }
            return responseContext;
        }

        @Override
        public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
            throw new UnsupportedOperationException("Asynchronous execution not supported.");
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
