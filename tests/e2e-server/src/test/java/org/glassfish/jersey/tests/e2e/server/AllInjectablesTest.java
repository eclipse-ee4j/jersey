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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import javax.inject.Singleton;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Paul Sandoz
 */
public class AllInjectablesTest extends JerseyTest {

    @Path("per-request")
    public static class PerRequestContextResource {
        @Context Application app;
        @Context ResourceContext rc;
        @Context Configuration config;
        @Context MessageBodyWorkers mbw;
        @Context HttpHeaders hs;
        @Context UriInfo ui;
        @Context ExtendedUriInfo eui;
        @Context Request r;
        @Context SecurityContext sc;
        @Context Providers p;

        @GET
        public String get() {
            assertNotNull(app);
            assertNotNull(rc);
            assertNotNull(config);
            assertNotNull(mbw);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }
    }

    @Path("per-request-constructor")
    public static class PerRequestContextConstructorParameterResource {
        public PerRequestContextConstructorParameterResource(@Context final Application app, @Context final ResourceContext rc,
                @Context final Configuration config, @Context final MessageBodyWorkers mbw, @Context final HttpHeaders hs,
                @Context final UriInfo ui, @Context final ExtendedUriInfo eui, @Context final Request r,
                @Context final SecurityContext sc, @Context final Providers p) {
            assertNotNull(app);
            assertNotNull(rc);
            assertNotNull(config);
            assertNotNull(mbw);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Path("per-request-method")
    public static class PerRequestContextMethodParameterResource {
        @GET
        public String get(@Context final Application app, @Context final ResourceContext rc, @Context final Configuration config,
                          @Context final MessageBodyWorkers mbw, @Context final HttpHeaders hs, @Context final UriInfo ui,
                          @Context final ExtendedUriInfo eui, @Context final Request r, @Context final SecurityContext sc,
                          @Context final Providers p) {
            assertNotNull(app);
            assertNotNull(rc);
            assertNotNull(config);
            assertNotNull(mbw);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }
    }

    @Path("singleton")
    @Singleton
    public static class SingletonContextResource {
        @Context Application app;
        @Context ResourceContext rc;
        @Context Configuration config;
        @Context MessageBodyWorkers mbw;
        @Context HttpHeaders hs;
        @Context UriInfo ui;
        @Context ExtendedUriInfo eui;
        @Context Request r;
        @Context SecurityContext sc;
        @Context Providers p;

        @GET
        public String get() {
            assertNotNull(app);
            assertNotNull(rc);
            assertNotNull(config);
            assertNotNull(mbw);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }
    }

    @Path("singleton-constructor")
    public static class SingletonContextConstructorParameterResource {
        public SingletonContextConstructorParameterResource(@Context final Application app, @Context final ResourceContext rc,
                @Context final Configuration config, @Context final MessageBodyWorkers mbw, @Context final HttpHeaders hs,
                @Context final UriInfo ui, @Context final ExtendedUriInfo eui, @Context final Request r,
                @Context final SecurityContext sc, @Context final Providers p) {
            assertNotNull(app);
            assertNotNull(rc);
            assertNotNull(config);
            assertNotNull(mbw);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Override
    public ResourceConfig configure() {
        return new ResourceConfig(PerRequestContextResource.class, PerRequestContextConstructorParameterResource.class,
                PerRequestContextMethodParameterResource.class, SingletonContextResource.class,
                SingletonContextConstructorParameterResource.class);
    }

    @Test
    public void testPerRequestInjected() throws IOException {
        assertEquals("GET", target().path("/per-request").request().get(String.class));
    }

    @Test
    public void testPerRequestConstructor() throws IOException {
        assertEquals("GET", target().path("/per-request-constructor").request().get(String.class));
    }

    @Test
    public void testPerRequestMethod() throws IOException {
        assertEquals("GET", target().path("/per-request-method").request().get(String.class));
    }

    @Test
    public void testSingleton() throws IOException {
        assertEquals("GET", target().path("/singleton").request().get(String.class));
    }

    @Test
    public void testSingletonConstructor() throws IOException {
        assertEquals("GET", target().path("/singleton-constructor").request().get(String.class));
    }

}
