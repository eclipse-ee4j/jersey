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

package org.glassfish.jersey.tests.e2e.server.filter;

import java.security.Principal;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
public class RolesAllowedTest extends JerseyTest {

    @PreMatching
    @Priority(Priorities.AUTHENTICATION)
    public static class SecurityFilter implements ContainerRequestFilter {

        public void filter(final ContainerRequestContext request) {
            final String user = request.getHeaders().getFirst("X-USER");
            request.setSecurityContext(new Authenticator(user));
        }

        private static class Authenticator implements SecurityContext {

            private final Principal principal;

            Authenticator(final String name) {
                principal = name == null
                        ? null
                        : new Principal() {
                            public String getName() {
                                return name;
                            }
                        };
            }

            public Principal getUserPrincipal() {
                return principal;
            }

            public boolean isUserInRole(final String role) {
                return role.equals(principal.getName()) || ("user".equals(role) && "admin".equals(principal.getName()));
            }

            public boolean isSecure() {
                return false;
            }

            public String getAuthenticationScheme() {
                return "";
            }
        }
    }

    @Path("/")
    @PermitAll
    public static class Resource {

        @RolesAllowed("user")
        @GET
        public String get() {
            return "GET";
        }

        @RolesAllowed("admin")
        @POST
        public String post(final String content) {
            return content;
        }

        @Path("sub")
        public SubResource getSubResource() {
            return new SubResource();
        }
    }

    @RolesAllowed("admin")
    public static class SubResource {

        @Path("deny-all")
        @DenyAll
        @GET
        public String denyAll() {
            return "GET";
        }

        @Path("permit-all")
        @PermitAll
        @GET
        public String permitAll() {
            return "GET";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, RolesAllowedDynamicFeature.class, SecurityFilter.class);
    }

    @Test
    public void testGetAsUser() {
        assertEquals("GET", target().request().header("X-USER", "user").get(String.class));
    }

    @Test
    public void testGetAsAdmin() {
        assertEquals("GET", target().request().header("X-USER", "admin").get(String.class));
    }

    @Test
    public void testPostAsUser() {
        final Response cr = target().request().header("X-USER", "user").post(Entity.text("POST"));
        assertEquals(403, cr.getStatus());
    }

    @Test
    public void testPostAsAdmin() {
        assertEquals("POST", target().request().header("X-USER", "admin").post(Entity.text("POST"), String.class));
    }

    @Test
    public void testDenyAll() {
        assertEquals(403, target("sub/deny-all").request().header("X-USER", "admin").get().getStatus());
    }

    @Test
    public void testPermitAll() {
        assertEquals("GET", target("sub/permit-all").request().header("X-USER", "xyz").get(String.class));
    }

    @Test
    public void testNotAuthorized() {
        assertThat("User should not be authorized.", target().request().get().getStatus(), is(403));
    }
}
