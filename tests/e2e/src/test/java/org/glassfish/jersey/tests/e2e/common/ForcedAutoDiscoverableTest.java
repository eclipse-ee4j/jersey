/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Note: Auto-discoverables from this test "affects" all other tests in suit.
 *
 * @author Michal Gajdos
 */
public class ForcedAutoDiscoverableTest extends JerseyTest {

    private static final String PROPERTY = "ForcedAutoDiscoverableTest";

    public static class CommonAutoDiscoverable implements ForcedAutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-common");

                    context.proceed();
                }
            }, 1);
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class ClientAutoDiscoverable implements ForcedAutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-client");

                    context.proceed();
                }
            }, 10);
        }
    }

    @ConstrainedTo(RuntimeType.SERVER)
    public static class ServerAutoDiscoverable implements ForcedAutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-server");

                    context.proceed();
                }
            }, 10);
        }
    }

    @Path("/")
    public static class Resource {

        @POST
        public String post(final String value) {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .property(PROPERTY, true)
                .property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_SERVER, true)
                .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_SERVER, true);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.property(PROPERTY, true)
        .property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT, true)
        .property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT, true);
    }

    @Test
    public void testForcedAutoDiscoverable() throws Exception {
        final Response response = target().request().post(Entity.text("value"));

        assertThat(response.readEntity(String.class), is("value-common-client-common-server"));
    }
}
