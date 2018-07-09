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

package org.glassfish.jersey.client.authentication;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.client.internal.LocalizationMessages;

/**
 * Implementation of Basic Http Authentication method (RFC 2617).
 *
 * @author Miroslav Fuksa
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Craig McClanahan
 */
final class BasicAuthenticator {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthenticator.class.getName());

    private final HttpAuthenticationFilter.Credentials defaultCredentials;

    /**
     * Creates a new instance of basic authenticator.
     *
     * @param defaultCredentials Credentials. Can be {@code null} if no default credentials should be
     *                           used.
     */
    BasicAuthenticator(HttpAuthenticationFilter.Credentials defaultCredentials) {
        this.defaultCredentials = defaultCredentials;
    }

    private String calculateAuthentication(HttpAuthenticationFilter.Credentials credentials) {
        String username = credentials.getUsername();
        byte[] password = credentials.getPassword();
        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = new byte[0];
        }

        final byte[] prefix = (username + ":").getBytes(HttpAuthenticationFilter.CHARACTER_SET);
        final byte[] usernamePassword = new byte[prefix.length + password.length];

        System.arraycopy(prefix, 0, usernamePassword, 0, prefix.length);
        System.arraycopy(password, 0, usernamePassword, prefix.length, password.length);

        return "Basic " + Base64.getEncoder().encodeToString(usernamePassword);
    }

    /**
     * Adds authentication information to the request.
     *
     * @param request Request context.
     */
    public void filterRequest(ClientRequestContext request) {
        HttpAuthenticationFilter.Credentials credentials = HttpAuthenticationFilter.getCredentials(request,
                defaultCredentials, HttpAuthenticationFilter.Type.BASIC);
        if (credentials == null) {
            LOGGER.fine(LocalizationMessages.AUTHENTICATION_CREDENTIALS_NOT_PROVIDED_BASIC());
        } else {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, calculateAuthentication(credentials));
        }
    }

    /**
     * Checks the response and if basic authentication is required then performs a new request
     * with basic authentication.
     *
     * @param request  Request context.
     * @param response Response context (will be updated with newest response data if the request was repeated).
     * @return {@code true} if response does not require authentication or if authentication is required,
     * new request was done with digest authentication information and authentication was successful.
     * @throws ResponseAuthenticationException in case that basic credentials missing or are in invalid format
     */
    public boolean filterResponseAndAuthenticate(ClientRequestContext request, ClientResponseContext response) {
        final String authenticate = response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
        if (authenticate != null && authenticate.trim().toUpperCase().startsWith("BASIC")) {
            HttpAuthenticationFilter.Credentials credentials = HttpAuthenticationFilter
                    .getCredentials(request, defaultCredentials, HttpAuthenticationFilter.Type.BASIC);

            if (credentials == null) {
                if (response.hasEntity()) {
                    AuthenticationUtil.discardInputAndClose(response.getEntityStream());
                }
                throw new ResponseAuthenticationException(null, LocalizationMessages.AUTHENTICATION_CREDENTIALS_MISSING_BASIC());
            }

            return HttpAuthenticationFilter.repeatRequest(request, response, calculateAuthentication(credentials));
        }
        return false;
    }
}
