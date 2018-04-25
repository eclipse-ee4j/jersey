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

import java.security.Principal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * End to end test class for testing security context in the Filter and
 * resource.
 *
 * @author Miroslav Fuksa
 */
public class SecurityContextFilterTest extends JerseyTest {

    private static final String PRINCIPAL_NAME = "test_principal_setByFilter";
    private static final String SKIP_FILTER = "skipFilter";
    private static final String PRINCIPAL_IS_NULL = "principalIsNull";

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(SecurityContextFilter.class, Resource.class);
    }

    /**
     * Test security context container request filter.
     */
    @PreMatching
    public static class SecurityContextFilter implements ContainerRequestFilter {

        // TODO: won't work until we have proxiable scope
//        @Context
//        SecurityContext securityContext;

        @Override
        public void filter(ContainerRequestContext context) {
            Assert.assertNotNull(context.getSecurityContext());

            // test injections
            // TODO: won't work until SecurityContext is proxiable
//            Assert.assertEquals(context.getSecurityContext(), securityContext);

            String header = context.getHeaders().getFirst(SKIP_FILTER);
            if ("true".equals(header)) {
                return;
            }

            context.setSecurityContext(new SecurityContext() {
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
                    };
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }
            });
        }
    }

    /**
     * Tests SecurityContext in filter.
     *
     * @throws Exception Thrown when request processing fails in the
     *                   application.
     */
    @Test
    public void testSecurityContextFilter() throws Exception {
        Response response = target().path("test").request().get();
        assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        assertEquals(PRINCIPAL_NAME, entity);
    }

    /**
     * Tests SecurityContext in filter.
     *
     * @throws Exception Thrown when request processing fails in the
     *                   application.
     */
    @Test
    public void testContainerSecurityContext() throws Exception {
        Response response = target().path("test").request().header(SKIP_FILTER, "true").get();
        assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        Assert.assertTrue(!entity.equals(PRINCIPAL_NAME));
    }

    /**
     * Test resource class.
     */
    @Path("test")
    public static class Resource {

        /**
         * Test resource method.
         *
         * @param crc container request context.
         * @return String response with principal name.
         */

        // TODO: inject SecurityContext directly once JERSEY-1282 is fixed
        @GET
        public String getPrincipal(@Context ContainerRequestContext crc) {
            Assert.assertNotNull(crc.getSecurityContext());
            Principal userPrincipal = crc.getSecurityContext().getUserPrincipal();
            return userPrincipal == null ? PRINCIPAL_IS_NULL : userPrincipal.getName();
        }
    }
}
