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

package org.glassfish.jersey.jsonp;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class JsonProcessingAutoDiscoverableServerTest {

    public static class Filter implements ContainerRequestFilter {

        @Context
        public Configuration config;

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            final Response response = Response
                    .status(400)
                    .entity(config.isEnabled(JsonProcessingFeature.class) ? "Enabled" : "Disabled")
                    .build();

            requestContext.abortWith(response);
        }
    }

    @Path("/")
    public static class Resource {

        @GET
        public String doGetFoo(@Context HttpHeaders headers) {
            return Integer.toString(headers.getLength());
        }
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerDefault() throws Exception {
        _test("Enabled", null, null);
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerEnabled() throws Exception {
        _test("Enabled", null, false);
    }

    @Test
    public void testAutoDiscoverableGlobalDefaultServerDisabled() throws Exception {
        _test("Disabled", null, true);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerDefault() throws Exception {
        _test("Disabled", true, null);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerEnabled() throws Exception {
        _test("Enabled", true, false);
    }

    @Test
    public void testAutoDiscoverableGlobalDisabledServerDisabled() throws Exception {
        _test("Disabled", true, true);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerDefault() throws Exception {
        _test("Enabled", false, null);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerEnabled() throws Exception {
        _test("Enabled", false, false);
    }

    @Test
    public void testAutoDiscoverableGlobalEnabledServerDisabled() throws Exception {
        _test("Disabled", false, true);
    }

    private void _test(final String response, final Boolean globalDisable, final Boolean serverDisable) throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, Filter.class);

        if (globalDisable != null) {
            resourceConfig.property(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, globalDisable);
        }
        if (serverDisable != null) {
            resourceConfig.property(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, serverDisable);
        }

        final ApplicationHandler app = new ApplicationHandler(resourceConfig);

        final URI baseUri = URI.create("/");
        assertEquals(response, app.apply(new ContainerRequest(baseUri, baseUri, "GET", null, new MapPropertiesDelegate())).get()
                .getEntity());
    }
}
