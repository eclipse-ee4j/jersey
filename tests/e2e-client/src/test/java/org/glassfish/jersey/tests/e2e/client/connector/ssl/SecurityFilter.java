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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * Simple authentication filter.
 *
 * Returns response with http status 401 when proper authentication is not provided in incoming request.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @see ContainerRequestFilter
 */
@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {

    /**
     * Security realm.
     */
    public static final String REALM = "Test HTTPS Authentication REALM";
    private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext filterContext) throws IOException {
        User user = authenticate(filterContext.getRequest());
        filterContext.setSecurityContext(new AuthorizationContext(user));
    }

    private User authenticate(Request request) {
        // Extract authentication credentials
        String authentication = ((ContainerRequest) request).getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authentication == null) {
            throw new AuthenticationException("Authentication credentials are required", REALM);
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
            // additional checks should be done here
            // "Only HTTP Basic authentication is supported"
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.getDecoder().decode(authentication)).split(":");
        if (values.length < 2) {
            throw new WebApplicationException(400);
            // "Invalid syntax for username and password"
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            throw new WebApplicationException(400);
            // "Missing username or password"
        }

        // Validate the extracted credentials
        User user;

        if ("user".equals(username) && "password".equals(password)) {
            user = new User("user", "user");
            LOGGER.info("USER AUTHENTICATED");
        } else {
            LOGGER.info("USER NOT AUTHENTICATED");
            throw new AuthenticationException("Invalid username or password", REALM);
        }
        return user;
    }

    private class AuthorizationContext implements SecurityContext {

        private final User user;
        private final Principal principal;

        public AuthorizationContext(final User user) {
            this.user = user;
            this.principal = new Principal() {

                @Override
                public String getName() {
                    return user.username;
                }
            };
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return (role.equals(user.role));
        }

        @Override
        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }

    private static class User {

        public String username;
        public String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
}
