/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.process;

import java.io.IOException;
import java.security.Principal;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.internal.inject.AbstractTest;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test if request scoped injection points are injected without using
 * dynamic proxies.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ProxyInjectablesTest extends AbstractTest {

    public static final Class<RequestContextBuilder.TestContainerRequest> REQUEST_CLASS = RequestContextBuilder
            .TestContainerRequest.class;

    private ContainerResponse resource(String uri) throws Exception {
        return apply(RequestContextBuilder.from(uri, "GET").build());
    }

    @Provider
    public static class SecurityContextFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(new MySecurityContext());
        }
    }

    private static class MySecurityContext implements SecurityContext {

        @Override
        public Principal getUserPrincipal() {
            return new Principal() {
                @Override
                public String getName() {
                    return "a";
                }
            };
        }

        @Override
        public boolean isUserInRole(String role) {
            return true;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return "BASIC";
        }
    }

    @Path("/")
    public static class PerRequestContextInjectedResource {

        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;

        @GET
        public String get() {
            assertEquals(UriRoutingContext.class, ui.getClass());
            assertEquals(REQUEST_CLASS, hs.getClass());
            assertEquals(REQUEST_CLASS, r.getClass());
            assertEquals(SecurityContextInjectee.class, sc.getClass());
            assertEquals("a", sc.getUserPrincipal().getName());
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonInjectedResource {

        @Inject
        UriInfo ui;

        @Inject
        HttpHeaders hs;

        @Inject
        Request r;

        @Inject
        SecurityContext sc;

        @GET
        public String get() {
            assertNotEquals(UriRoutingContext.class, ui.getClass());
            assertNotEquals(REQUEST_CLASS, hs.getClass());
            assertNotEquals(REQUEST_CLASS, r.getClass());
            assertNotEquals(MySecurityContext.class, sc.getClass());
            assertEquals("a", sc.getUserPrincipal().getName());
            return "GET";
        }
    }

    @Path("/")
    public static class PerRequestContextMethodParameterResource {

        @GET
        public String get(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc) {
            assertEquals(UriRoutingContext.class, ui.getClass());
            assertEquals(REQUEST_CLASS, hs.getClass());
            assertEquals(REQUEST_CLASS, r.getClass());
            assertEquals(SecurityContextInjectee.class, sc.getClass());
            assertEquals("a", sc.getUserPrincipal().getName());
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonContextMethodParameterResource {

        @GET
        public String get(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc) {
            assertEquals(UriRoutingContext.class, ui.getClass());
            assertEquals(REQUEST_CLASS, hs.getClass());
            assertEquals(REQUEST_CLASS, r.getClass());
            assertEquals(SecurityContextInjectee.class, sc.getClass());
            assertEquals("a", sc.getUserPrincipal().getName());
            return "GET";
        }
    }

    @Test
    public void testPerRequestContextInjected() throws Exception {
        initiateWebApplication(PerRequestContextInjectedResource.class, SecurityContextFilter.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestInjectInjected() throws Exception {
        initiateWebApplication(SingletonInjectedResource.class, SecurityContextFilter.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestMethodParameterInjected() throws Exception {
        initiateWebApplication(PerRequestContextMethodParameterResource.class, SecurityContextFilter.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testSingletonParameterInjected() throws Exception {
        initiateWebApplication(SingletonContextMethodParameterResource.class, SecurityContextFilter.class);

        assertEquals("GET", resource("/").getEntity());
    }

    /**
     * Part of JERSEY-2386 reproducer. The request field
     * should not get injected as a dynamic proxy.
     */
    public static class MyFieldInjectedBean {

        @Context
        Request request;
    }

    /**
     * Part of JERSEY-2386 reproducer. The request field
     * should not get injected as a dynamic proxy.
     */
    public static class MyCtorInjectedBean {

        /**
         * This should get directly injected.
         */
        public MyCtorInjectedBean(@Context Request request) {
            this.request = request;
        }

        Request request;
    }

    /**
     * JERSEY-2386 reproducer. Bean parameter below must
     * get injected directly as well as its internal field.
     */
    @Path("/")
    public static class BeanParamInjectionResource {

        @GET
        @Path("field")
        public String getViaField(@BeanParam MyFieldInjectedBean bean) {
            assertEquals(MyFieldInjectedBean.class, bean.getClass());
            assertEquals(REQUEST_CLASS, bean.request.getClass());
            return "field";
        }

        @GET
        @Path("ctor")
        public String getViaCtor(@BeanParam MyCtorInjectedBean bean) {
            assertEquals(MyCtorInjectedBean.class, bean.getClass());
            assertEquals(REQUEST_CLASS, bean.request.getClass());
            return "ctor";
        }
    }

    /**
     * JERSEY-2386 reproducer. Make sure no dynamic proxy gets involved
     * when injecting into a bean parameter.
     */
    @Test
    public void testBeanParam() throws Exception {
        initiateWebApplication(BeanParamInjectionResource.class);

        assertEquals("field", resource("/field").getEntity());
        assertEquals("ctor", resource("/ctor").getEntity());
    }
}
