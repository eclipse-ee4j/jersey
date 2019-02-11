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

package org.glassfish.jersey.client;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.internal.util.collection.UnsafeValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link ClientConfig} unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ClientConfigTest {

    public ClientConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSnapshot() {
        ClientConfig c_a = new ClientConfig().property("common_name", "common_value");

        ClientConfig c_b = c_a.snapshot();
        assertNotNull(c_b);
        assertNotSame(c_a, c_b);
        assertEquals(c_a, c_b);
        assertEquals("common_value", c_a.getProperty("common_name"));
        assertEquals("common_value", c_b.getProperty("common_name"));

        c_b.property("name", "value");

        assertFalse(c_a.equals(c_b));
        assertEquals("value", c_b.getProperty("name"));
        assertNull(c_a.getProperty("name"));
    }

    @Test
    public void testGetProperties() {
        ClientConfig instance = new ClientConfig();
        Map<String, Object> result = instance.getProperties();
        assertNotNull(result);

        instance.property("name", "value");
        assertEquals("value", result.get("name"));

        try {
            result.remove("name");
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ex) {
            // ok
        }
    }

    @Test
    public void testGetProperty() {
        ClientConfig instance = new ClientConfig().property("name", "value");
        assertEquals("value", instance.getProperty("name"));
        assertNull(instance.getProperty("other"));
    }

    @Provider
    public class MyProvider implements ContextResolver<String> {

        @Override
        public String getContext(final Class<?> type) {
            return "";
        }
    }

    @Test
    public void testCustomProvidersFeature() {
        final CustomProvidersFeature feature = new CustomProvidersFeature(Arrays.asList(new Class<?>[]{MyProvider.class}));

        ClientConfig instance = new ClientConfig();
        instance.register(feature);

        assertTrue(instance.getClasses().isEmpty());
        assertEquals(1, instance.getInstances().size());

        // Features are registered at the time of provider bindings.
        final JerseyClient jerseyClient = new JerseyClient(instance, (UnsafeValue<SSLContext, IllegalStateException>) null, null);
        ClientConfig config = jerseyClient.getConfiguration();
        final ClientRuntime runtime = config.getRuntime();

        final ClientConfig runtimeConfig = runtime.getConfig();

        assertTrue(runtimeConfig.isRegistered(MyProvider.class));
        assertTrue(runtimeConfig.isEnabled(feature));

    }

    public static class EmptyFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    public static class UnconfigurableFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            return false;
        }
    }

    /**
     * Copied from CommonConfigTest.
     */
    @Test
    public void testGetFeatures() {
        final EmptyFeature emptyFeature = new EmptyFeature();
        final UnconfigurableFeature unconfigurableFeature = new UnconfigurableFeature();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(emptyFeature);
        clientConfig.register(unconfigurableFeature);

        // Features are registered at the time of provider bindings.
        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, (UnsafeValue<SSLContext, IllegalStateException>) null, null);
        clientConfig = jerseyClient.getConfiguration();
        clientConfig.getRuntime();

        final Configuration runtimeConfig = clientConfig.getRuntime().getConfig();

        assertTrue(runtimeConfig.isEnabled(emptyFeature));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetProviderClasses() {
        ClientConfig instance = new ClientConfig();
        instance.getClasses().add(ClientConfigTest.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetProviderInstances() {
        ClientConfig instance = new ClientConfig();
        instance.getInstances().add(this);
    }

    @Test
    public void testUpdate() {
        final UnconfigurableFeature unconfigurableFeature = new UnconfigurableFeature();

        ClientConfig clientConfig1 = new ClientConfig();
        ClientConfig clientConfig2 = new ClientConfig();

        clientConfig1.register(EmptyFeature.class);
        clientConfig2.register(unconfigurableFeature);

        ClientConfig clientConfig = clientConfig2.loadFrom(clientConfig1);

        // Features are registered at the time of provider bindings.
        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, (UnsafeValue<SSLContext, IllegalStateException>) null, null);
        clientConfig = jerseyClient.getConfiguration();
        clientConfig.getRuntime();

        final Configuration runtimeConfig = clientConfig.getRuntime().getConfig();

        assertTrue(runtimeConfig.isEnabled(EmptyFeature.class));
    }

    @Test
    public void testSetProperty() {
        ClientConfig instance = new ClientConfig();
        assertTrue(instance.getProperties().isEmpty());

        instance.property("name", "value");
        assertFalse(instance.getProperties().isEmpty());
        assertEquals(1, instance.getProperties().size());
        assertEquals("value", instance.getProperty("name"));
    }
}
