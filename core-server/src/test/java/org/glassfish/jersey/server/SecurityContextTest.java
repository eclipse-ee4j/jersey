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

package org.glassfish.jersey.server;

import java.io.IOException;
import java.security.Principal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import javax.annotation.Priority;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for testing security context in the Filter and resource.
 *
 * @author Miroslav Fuksa
 */
public class SecurityContextTest {

    private static final String PRINCIPAL_NAME = "SetByFilter";
    private static final String PRINCIPAL_NAME_SECOND = "SetByFilterSecond";
    private static final String SKIP_FILTER = "skipFilter";
    private static final String PRINCIPAL_IS_NULL = "principalIsNull";

    @Priority(100)
    private static class SecurityContextFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext rc) throws IOException {
            // test injections
            assertNotNull(rc.getSecurityContext());
            assertTrue(rc.getSecurityContext().getUserPrincipal() == null);

            String header = rc.getHeaders().getFirst(SKIP_FILTER);
            if ("true".equals(header)) {
                return;
            }

            // set new Security Context
            rc.setSecurityContext(new SecurityContext() {

                @Override
                public boolean isUserInRole(String role) {
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {

                        @Override
                        public String getName() {
                            return PRINCIPAL_NAME;
                        }

                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object obj) {
                            return (obj instanceof Principal)
                                    && PRINCIPAL_NAME.equals(((Principal) obj).getName());
                        }

                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }

                @Override
                public int hashCode() {
                    return super.hashCode();
                }

                @Override
                public boolean equals(Object that) {
                    return (that != null && that.getClass() == this.getClass());
                }

                @Override
                public String toString() {
                    return super.toString();
                }


            });
        }
    }


    @Priority(101)
    private static class SecurityContextFilterSecondInChain implements ContainerRequestFilter {
        @Context
        SecurityContext sc;

        @Override
        public void filter(ContainerRequestContext rc) throws IOException {
            assertNotNull(sc);
            assertEquals(sc.getUserPrincipal().getName(), PRINCIPAL_NAME);
            assertEquals(sc, rc.getSecurityContext());

            String header = rc.getHeaders().getFirst(SKIP_FILTER);
            if ("true".equals(header)) {
                return;
            }

            // set new Security Context
            rc.setSecurityContext(new SecurityContext() {

                @Override
                public boolean isUserInRole(String role) {
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {

                        @Override
                        public String getName() {
                            return PRINCIPAL_NAME_SECOND;
                        }

                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object obj) {
                            return (obj instanceof Principal)
                                    && PRINCIPAL_NAME_SECOND.equals(((Principal) obj).getName());
                        }

                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }

                @Override
                public int hashCode() {
                    return super.hashCode();
                }

                @Override
                public boolean equals(Object that) {
                    return (that != null && that.getClass() == this.getClass());
                }

                @Override
                public String toString() {
                    return super.toString();
                }
            });
        }
    }

    /**
     * Tests SecurityContext injection into a resource method.
     *
     * @throws Exception Thrown when request processing fails in the application.
     */
    @Test
    public void testSecurityContextInjection() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, SecurityContextFilter.class,
                SecurityContextFilterSecondInChain.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerResponse response = application.apply(RequestContextBuilder.from("/test/2", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals(PRINCIPAL_NAME_SECOND, response.getEntity());
    }

    /**
     * Tests SecurityContext in filter.
     *
     * @throws Exception Thrown when request processing fails in the application.
     */
    @Test
    public void testSecurityContextInjectionFilter() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, SecurityContextFilter.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerResponse response = application.apply(RequestContextBuilder.from("/test", "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals(PRINCIPAL_NAME, response.getEntity());
    }

    /**
     * Tests SecurityContext in filter.
     *
     * @throws Exception Thrown when request processing fails in the
     *                   application.
     */
    @Test
    public void testDefaultSecurityContext() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class, SecurityContextFilter.class);
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        ContainerResponse response =
                application.apply(RequestContextBuilder.from("/test", "GET").header(SKIP_FILTER, "true").build()).get();
        assertEquals(200, response.getStatus());
        Object entity = response.getEntity();
        assertTrue(!PRINCIPAL_NAME.equals(entity));
    }

    /**
     * Test resource class.
     */
    @Path("test")
    public static class Resource {

        /**
         * Test resource method.
         *
         * @param cr Container request context.
         * @return String response with principal name.
         */
        @GET
        public String getSomething(@Context ContainerRequestContext cr) {
            assertNotNull(cr.getSecurityContext());
            Principal userPrincipal = cr.getSecurityContext().getUserPrincipal();
            return userPrincipal == null ? PRINCIPAL_IS_NULL : userPrincipal.getName();
        }

        /**
         * Test resource method.
         *
         * @param sc security context.
         * @param cr container request context.
         * @return String response with principal name.
         */
        @GET
        @Path("2")
        public String getSomething2(@Context SecurityContext sc, @Context ContainerRequestContext cr) {
            assertEquals(sc, cr.getSecurityContext());
            Principal userPrincipal = sc.getUserPrincipal();
            return userPrincipal == null ? PRINCIPAL_IS_NULL : userPrincipal.getName();
        }
    }
}
