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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import javax.inject.Inject;

import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class RuntimeConfigTest extends JerseyTest {

    @Path("/")
    public static class Resource {

        @GET
        public String get() {
            return "get";
        }
    }

    public static class EmptyFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    public static class ClientFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            context.register(ClientReaderInterceptor.class);
            context.property("foo", "bar");
            return true;
        }
    }

    public static class ClientReaderInterceptor implements ReaderInterceptor {

        private final Configuration config;

        @Inject
        public ClientReaderInterceptor(final Configuration configuration) {
            this.config = configuration;
        }

        @Override
        public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
            assertTrue(config.isRegistered(ClientFeature.class));
            assertTrue(config.isRegistered(ClientReaderInterceptor.class));

            assertThat(config.getProperties().size(), is(2));
            assertThat(config.getProperty("foo").toString(), is("bar"));

            // JsonFeature
            assertThat(config.getProperty(InternalProperties.JSON_FEATURE_CLIENT), notNullValue());

            // MetaInfAutoDiscoverable
            assertThat(config.getInstances().size(), is(1));
            assertTrue(config.isEnabled(ClientFeature.class));

            context.getHeaders().add("CustomHeader", "ClientReaderInterceptor");

            return context.proceed();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testRuntimeClientConfig() throws Exception {
        final WebTarget target = target();

        target.register(ClientFeature.class);

        final Response response = target.request(MediaType.WILDCARD_TYPE).get(Response.class);

        assertEquals(1, target.getConfiguration().getClasses().size());
        assertTrue(target.getConfiguration().isRegistered(ClientFeature.class));
        assertTrue(target.getConfiguration().getInstances().isEmpty());
        assertTrue(target.getConfiguration().getProperties().isEmpty());
        assertFalse(target.getConfiguration().isEnabled(ClientFeature.class));

        WebTarget t = target();
        assertEquals(0, t.getConfiguration().getClasses().size());
        assertFalse(t.getConfiguration().isRegistered(ClientFeature.class));
        assertTrue(t.getConfiguration().getInstances().isEmpty());
        assertTrue(t.getConfiguration().getProperties().isEmpty());
        assertFalse(t.getConfiguration().isEnabled(ClientFeature.class));

        assertEquals("get", response.readEntity(String.class));
        assertEquals("ClientReaderInterceptor", response.getHeaderString("CustomHeader"));
    }
}
