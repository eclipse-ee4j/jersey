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

package org.glassfish.jersey.client;

import java.io.IOException;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import javax.annotation.Priority;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Note: Auto-discoverables from this test "affects" all other tests in suit.
 *
 * @author Michal Gajdos
 */
public class AutoDiscoverableClientTest {

    private static final String PROPERTY = "AutoDiscoverableTest";

    private static final ClientRequestFilter component = new ClientRequestFilter() {
        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.status(400).entity("CommonAutoDiscoverable").build());
        }
    };

    public static class CommonAutoDiscoverable implements AutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(component, 1);
        }
    }

    @Priority(10)
    public static class AbortFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.status(400).entity("AbortFilter").build());
        }
    }

    public static class FooLifecycleListener implements ContainerRequestFilter, ClientLifecycleListener {
        private static boolean CLOSED = false;
        private static boolean INITIALIZED = false;

        @Override
        public void onInit() {
            INITIALIZED = true;
        }

        @Override
        public void onClose() {
            CLOSED = true;
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            // do nothing
        }

        public static boolean isClosed() {
            return CLOSED;
        }

        public static boolean isInitialized() {
            return INITIALIZED;
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class LifecycleListenerAutoDiscoverable implements AutoDiscoverable {
        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }
            context.register(new FooLifecycleListener(), 1);
        }
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerDefault() throws Exception {
        _test("CommonAutoDiscoverable", null, null);
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerEnabled() throws Exception {
        _test("CommonAutoDiscoverable", null, false);
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerDisabled() throws Exception {
        _test("AbortFilter", null, true);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerDefault() throws Exception {
        _test("AbortFilter", true, null);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerEnabled() throws Exception {
        _test("CommonAutoDiscoverable", true, false);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerDisabled() throws Exception {
        _test("AbortFilter", true, true);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerDefault() throws Exception {
        _test("CommonAutoDiscoverable", false, null);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerEnabled() throws Exception {
        _test("CommonAutoDiscoverable", false, false);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerDisabled() throws Exception {
        _test("AbortFilter", false, true);
    }

    /**
     * Tests, that {@link org.glassfish.jersey.client.ClientLifecycleListener} registered via
     * {@link org.glassfish.jersey.internal.spi.AutoDiscoverable}
     * {@link javax.ws.rs.core.Feature} will be notified when {@link javax.ws.rs.client.Client#close()} is invoked.
     */
    @Test
    @Ignore("intermittent failures.")
    public void testAutoDiscoverableClosing() {
        final ClientConfig config = new ClientConfig();
        config.property(PROPERTY, true);
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient(config);

        assertFalse(FooLifecycleListener.isClosed());

        client.getConfiguration().getRuntime(); // force runtime init
        assertTrue("FooLifecycleListener was expected to be already initialized.", FooLifecycleListener.isInitialized());
        assertFalse("FooLifecycleListener was not expected to be closed yet.", FooLifecycleListener.isClosed());

        client.close();

        assertTrue("FooLifecycleListener should have been closed.", FooLifecycleListener.isClosed());
    }

    private void _test(final String response, final Boolean globalDisable, final Boolean clientDisable) throws Exception {
        final ClientConfig config = new ClientConfig();
        config.register(AbortFilter.class);
        config.property(PROPERTY, true);

        if (globalDisable != null) {
            config.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, globalDisable);
        }
        if (clientDisable != null) {
            config.property(ClientProperties.FEATURE_AUTO_DISCOVERY_DISABLE, clientDisable);
        }

        final Client client = ClientBuilder.newClient(config);
        final Invocation.Builder request = client.target("").request();

        assertEquals(response, request.get().readEntity(String.class));
    }
}
