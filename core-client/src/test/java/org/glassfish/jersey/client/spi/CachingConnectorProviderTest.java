/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Caching connector provider unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class CachingConnectorProviderTest {
    public static class ReferenceCountingNullConnector implements Connector, ConnectorProvider {

        private static final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public ClientResponse apply(ClientRequest request) {
            throw new ProcessingException("test");
        }

        @Override
        public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
            throw new ProcessingException("test-async");
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Connector getConnector(Client client, Configuration runtimeConfig) {
            counter.incrementAndGet();
            return this;
        }

        public int getCount() {
            return counter.get();
        }
    }

    @Test
    public void testCachingConnector() {
        final ReferenceCountingNullConnector connectorProvider = new ReferenceCountingNullConnector();
        final CachingConnectorProvider cachingConnectorProvider = new CachingConnectorProvider(connectorProvider);
        final ClientConfig configuration = new ClientConfig().connectorProvider(cachingConnectorProvider).getConfiguration();

        Client client1 = ClientBuilder.newClient(configuration);
        try {
            client1.target(UriBuilder.fromUri("/").build()).request().get();
        } catch (ProcessingException ce) {
            assertEquals("test", ce.getMessage());
            assertEquals(1, connectorProvider.getCount());
        }
        try {
            client1.target(UriBuilder.fromUri("/").build()).request().async().get();
        } catch (ProcessingException ce) {
            assertEquals("test-async", ce.getMessage());
            assertEquals(1, connectorProvider.getCount());
        }

        Client client2 = ClientBuilder.newClient(configuration);
        try {
            client2.target(UriBuilder.fromUri("/").build()).request().get();
        } catch (ProcessingException ce) {
            assertEquals("test", ce.getMessage());
            assertEquals(1, connectorProvider.getCount());
        }
        try {
            client2.target(UriBuilder.fromUri("/").build()).request().async().get();
        } catch (ProcessingException ce) {
            assertEquals("test-async", ce.getMessage());
            assertEquals(1, connectorProvider.getCount());
        }
    }
}
