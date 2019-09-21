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

package org.glassfish.jersey.client.oauth2;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

import javax.annotation.Priority;

/**
 * Client filter that adds access token to the {@code Authorization} http header. The filter uses {@code bearer}
 * token specification.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
@Priority(Priorities.AUTHENTICATION)
class OAuth2ClientFilter implements ClientRequestFilter {

    private final String accessToken;

    /**
     * Create a new filter with predefined access token.
     *
     * @param accessToken Access token.
     */
    public OAuth2ClientFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Create a new filter with no default access token. The token must be specified with
     * each request using {@link OAuth2ClientSupport#OAUTH2_PROPERTY_ACCESS_TOKEN}.
     */
    public OAuth2ClientFilter() {
        this.accessToken = null;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        String token = this.accessToken;
        final String propertyToken = (String) request.getProperty(OAuth2ClientSupport.OAUTH2_PROPERTY_ACCESS_TOKEN);
        if (propertyToken != null) {
            token = propertyToken;
        }
        request.removeProperty(OAuth2ClientSupport.OAUTH2_PROPERTY_ACCESS_TOKEN);
        if (token == null) {
            return;
        }
        String authentication = "Bearer " + token;

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, authentication);
        }

    }
}
