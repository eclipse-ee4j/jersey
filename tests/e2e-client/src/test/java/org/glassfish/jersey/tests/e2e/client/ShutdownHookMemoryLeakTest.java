/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
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
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Ensure Jersey connectors do not leak resources
 * in case multiple client runtime instances are being created.
 *
 * On my laptop, 1000 iterations was sufficient to cause
 * a memory leak until JERSEY-2688 got fixed.
 *
 * @author Jakub Podlesak
 */
public class ShutdownHookMemoryLeakTest {

    private static final String PATH = "test";
    private static final int ITERATIONS = 1000;

    public static List<ConnectorProvider> connectionProviders() {
        return Arrays.asList(
                new GrizzlyConnectorProvider(),
                new JettyConnectorProvider(),
                new ApacheConnectorProvider(),
                new HttpUrlConnectorProvider()
        );
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        connectionProviders().forEach(connectionProvider -> {
            ShutdownHookMemoryLeakTemplateTest test = new ShutdownHookMemoryLeakTemplateTest(connectionProvider) {};
            tests.add(TestHelper.toTestContainer(test, connectionProvider.getClass().getSimpleName()));
        });
        return tests;
    }

    @Path(PATH)
    public static class TestResource {

        @GET
        public String get() {
            return "GET";
        }
    }

    public abstract static class ShutdownHookMemoryLeakTemplateTest extends JerseyTest {
        private final ConnectorProvider connectorProvider;

        public ShutdownHookMemoryLeakTemplateTest(final ConnectorProvider cp) {
            connectorProvider = cp;
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
        @Disabled("Unstable, ignored for now")
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

//            the following check is fragile, as GC could break it easily
//            assertThat(String.format(
//                    "%s: number of closed listeners should correspond to the number of missing hooks",
//                            connectorProvider.getClass()),
//                     listenersClosed.get(), is(ITERATIONS - shutdownHooks.size()));

            client().close();      // clean up the rest

            assertThat(String.format(
                            "%s: number of closed listeners should be the same as the number of total requests made",
                            connectorProvider.getClass()),
                    listenersClosed.get(), is(ITERATIONS));
        }

        private Collection getShutdownHooks(javax.ws.rs.client.Client client)
                throws NoSuchFieldException, IllegalAccessException {
            JerseyClient jerseyClient = (JerseyClient) client;
            Field shutdownHooksField = JerseyClient.class.getDeclaredField("shutdownHooks");
            shutdownHooksField.setAccessible(true);
            return (Collection) shutdownHooksField.get(jerseyClient);
        }
    }
}
