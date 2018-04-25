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

import java.util.Collection;
import java.util.Map;

/**
 * Class that contains a result of the Authorization Flow including a access token.
 * <p>
 * All result properties can be get by the method {@link #getAllProperties()}. Some of the properties
 * are standardized by the OAuth 2 specification and therefore the class contains getters that extract
 * these properties from the property map.
 * </p>
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public class TokenResult {

    private final Map<String, Object> properties;

    /**
     * Create a new instance initiated from the property map.
     * @param properties Access properties.
     */
    public TokenResult(final Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Get access token.
     *
     * @return Access token.
     */
    public String getAccessToken() {
        return getProperty("access_token");
    }

    /**
     * Get expiration time of the {@link #getAccessToken() access token} in seconds.
     *
     * @return Expiration time in seconds or {@code null} if the value is not provided.
     */
    public Long getExpiresIn() {
        final String expiration = getProperty("expires_in");
        if (expiration == null) {
            return null;
        }
        return Long.valueOf(expiration);
    }

    /**
     * Get the refresh token. Note that the refresh token must not be issued during the authorization flow.
     * Some Service Providers issue refresh token only on first user authorization and some providers
     * does not support refresh token at all and authorization flow must be always performed when token
     * expires.
     *
     * @return Refresh token or {@code null} if the value is not provided.
     */
    public String getRefreshToken() {
        return getProperty("refresh_token");
    }

    /**
     * Get the type of the returned access token. Type is in most cases {@code bearer} (no cryptography is used)
     * but provider might support also other kinds of token like {@code mac}.
     *
     * @return Token type.
     */
    public String getTokenType() {
        return getProperty("token_type");
    }

    /**
     * Get the map of all properties returned in the Access Token Response.
     *
     * @return Map with all token properties.
     */
    public Map<String, Object> getAllProperties() {
        return properties;
    }

    private String getProperty(final String name) {
        final Object property = properties.get(name);

        if (property != null) {
            if (property instanceof Collection) {
                for (final Object value : (Collection) property) {
                    if (value != null) {
                        return value.toString();
                    }
                }
            } else {
                return property.toString();
            }
        }

        return null;
    }
}
