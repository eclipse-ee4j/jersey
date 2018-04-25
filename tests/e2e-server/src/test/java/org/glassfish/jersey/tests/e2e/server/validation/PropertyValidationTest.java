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

package org.glassfish.jersey.tests.e2e.server.validation;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.validation.constraints.NotNull;

import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationFeature;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test various combinations of enabling/disabling: auto-discovery, bean validation, validation feature.
 *
 * @author Michal Gajdos
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class PropertyValidationTest {

    @Path("/")
    public static class Resource {

        @POST
        @NotNull
        public String post(final String value) {
            return value.isEmpty() ? null : value;
        }
    }

    @Test
    public void testDefaultValidationDefaultDiscoveryFeature() throws Exception {
        _test(500, null, null, true);
    }

    @Test
    public void testDefaultValidationDefaultDiscoveryNoFeature() throws Exception {
        _test(500, null, null, false);
    }

    @Test
    public void testDefaultValidationDiscoveryFeature() throws Exception {
        _test(500, null, false, true);
    }

    @Test
    public void testDefaultValidationDiscoveryNoFeature() throws Exception {
        _test(500, null, false, false);
    }

    @Test
    public void testDefaultValidationNoDiscoveryFeature() throws Exception {
        _test(500, null, true, true);
    }

    @Test
    public void testDefaultValidationNoDiscoveryNoFeature() throws Exception {
        // Even though properties are disabled BV is registered.
        _test(500, null, true, false);
    }

    @Test
    public void testValidationDefaultDiscoveryFeature() throws Exception {
        _test(500, false, null, true);
    }

    @Test
    public void testValidationDefaultDiscoveryNoFeature() throws Exception {
        _test(500, false, null, false);
    }

    @Test
    public void testValidationDiscoveryFeature() throws Exception {
        _test(500, false, false, true);
    }

    @Test
    public void testValidationDiscoveryNoFeature() throws Exception {
        _test(500, false, false, false);
    }

    @Test
    public void testValidationNoDiscoveryFeature() throws Exception {
        _test(500, false, true, true);
    }

    @Test
    public void testValidationNoDiscoveryNoFeature() throws Exception {
        // Even though properties are disabled BV is registered.
        _test(500, false, true, false);
    }

    @Test
    public void testNoValidationDefaultDiscoveryFeature() throws Exception {
        _test(204, true, null, true);
    }

    @Test
    public void testNoValidationDefaultDiscoveryNoFeature() throws Exception {
        _test(204, true, null, false);
    }

    @Test
    public void testNoValidationDiscoveryFeature() throws Exception {
        _test(204, true, false, true);
    }

    @Test
    public void testNoValidationDiscoveryNoFeature() throws Exception {
        _test(204, true, false, false);
    }

    @Test
    public void testNoValidationNoDiscoveryFeature() throws Exception {
        _test(204, true, true, true);
    }

    @Test
    public void testNoValidationNoDiscoveryNoFeature() throws Exception {
        _test(204, true, true, false);
    }

    private void _test(final int responseStatus, final Boolean disableValidation,
                       final Boolean disableAutoDiscovery, final boolean registerFeature) throws Exception {
        final URI uri = URI.create("/");

        assertApply(responseStatus, initResourceConfig(disableValidation, disableAutoDiscovery, registerFeature), uri);

        if (responseStatus == 500) {
            // validation works - environment is validation friendly -> let's try to disable META-INF/services lookup
            final ResourceConfig resourceConfig = initResourceConfig(disableValidation, disableAutoDiscovery, true);
            resourceConfig.property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);

            assertApply(500, resourceConfig, uri);
        }
    }

    private ResourceConfig initResourceConfig(final Boolean disableValidation,
                                              final Boolean disableAutoDiscovery, final boolean registerFeature) {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class).register(LoggingFeature.class);

        if (registerFeature) {
            resourceConfig.register(ValidationFeature.class);
        }
        if (disableAutoDiscovery != null) {
            resourceConfig.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, disableAutoDiscovery);
        }
        if (disableValidation != null) {
            resourceConfig.property(ServerProperties.BV_FEATURE_DISABLE, disableValidation);
        }
        return resourceConfig;
    }

    private void assertApply(int responseStatus, ResourceConfig resourceConfig, URI uri)
            throws InterruptedException, ExecutionException {
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);
        final ContainerRequest requestContext = new ContainerRequest(uri, uri, "POST", null, new MapPropertiesDelegate());
        final ContainerResponse containerResponse = applicationHandler.apply(requestContext).get();

        assertEquals(responseStatus, containerResponse.getStatus());
    }

}
