/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientLifecycleListener;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Ensure Jersey connectors do not leak resources
 * in case multiple client runtime instances are being created.
 *
 * On my laptop, 1000 iterations was sufficient to cause
 * a memory leak until JERSEY-2688 got fixed.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class ShutdownHookMemoryLeakTest extends JerseyTest {

    private static final String PATH = "test";
    private static final int ITERATIONS = 1000;

    private final ConnectorProvider connectorProvider;

    public ShutdownHookMemoryLeakTest(final ConnectorProvider cp) {
        connectorProvider = cp;
    }


    @Parameterized.Parameters
    public static List<ConnectorProvider[]> connectionProviders() {
        return Arrays.asList(new ConnectorProvider[][] {
                {new GrizzlyConnectorProvider()},
                {new JettyConnectorProvider()},
                {new ApacheConnectorProvider()},
                {new HttpUrlConnectorProvider()}
        });
    }

    @Path(PATH)
    public static class TestResource {

        @GET
        public String get() {
            return "GET";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(connectorProvider);
    }

    @Test
    @Ignore("Unstable, ignored for now")
    public void testClientDoesNotLeakResources() throws Exception {

        final AtomicInteger listenersInitialized = new AtomicInteger(0);
        final AtomicInteger listenersClosed = new AtomicInteger(0);

        for (int i = 0; i < ITERATIONS; i++) {
            final Response response = target(PATH).property("another", "runtime").register(new ClientLifecycleListener() {
                @Override
                public void onInit() {
                    listenersInitialized.incrementAndGet();
                }

                @Override
                public void onClose() {
                    listenersClosed.incrementAndGet();
                }
            }).register(LoggingFeature.class).request().get();
            assertEquals("GET", response.readEntity(String.class));
        }

        Collection shutdownHooks = getShutdownHooks(client());

        assertThat(String.format(
                    "%s: number of initialized listeners should be the same as number of total request count",
                        connectorProvider.getClass()),
                listenersInitialized.get(), is(ITERATIONS));

//      the following check is fragile, as GC could break it easily
//        assertThat(String.format(
//                "%s: number of closed listeners should correspond to the number of missing hooks",
//                        connectorProvider.getClass()),
//                listenersClosed.get(), is(ITERATIONS - shutdownHooks.size()));

        client().close();      // clean up the rest

        assertThat(String.format(
                        "%s: number of closed listeners should be the same as the number of total requests made",
                        connectorProvider.getClass()),
                listenersClosed.get(), is(ITERATIONS));
    }

    private Collection getShutdownHooks(javax.ws.rs.client.Client client) throws NoSuchFieldException, IllegalAccessException {
        JerseyClient jerseyClient = (JerseyClient) client;
        Field shutdownHooksField = JerseyClient.class.getDeclaredField("shutdownHooks");
        shutdownHooksField.setAccessible(true);
        return (Collection) shutdownHooksField.get(jerseyClient);
    }
}
