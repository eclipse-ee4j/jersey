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

/**
 * Class that contains definition od parameters used in OAuth2.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public final class OAuth2Parameters {
    /**
     * Parameter {@code client_id} that
     * corresponds to ({@link org.glassfish.jersey.client.oauth2.ClientIdentifier#getClientId()}).
     */
    public static final String CLIENT_ID = "client_id";

    /**
     * Parameter {@code client_secret} that
     * corresponds to ({@link ClientIdentifier#getClientSecret()}).
     */
    public static final String CLIENT_SECRET = "client_secret";

    /**
     * Parameter {@code response_type} used in the
     * authorization request. For Authorization Code Grant Flow the value is {@code code}.
     */
    public static final String RESPONSE_TYPE = "response_type";

    /**
     * Parameter {@code response_type} used in the
     * authorization request.
     */
    public static final String REDIRECT_URI = "redirect_uri";

    /**
     * Parameter {@code scope} that defines the scope to which an authorization is requested.
     * Space delimited format. Scope values are defined by the Service Provider.
     */
    public static final String SCOPE = "scope";
    /**
     * State parameter used in the authorization request and authorization
     * response to protect against CSRF attacks.
     */
    public static final String STATE = "state";

    /**
     * Parameter {@code refresh_token} contains Refresh Token (corresponds
     * to {@link org.glassfish.jersey.client.oauth2.TokenResult#getRefreshToken()}).
     */
    public static final String REFRESH_TOKEN = "refresh_token";
    /**
     * Authorization code
     */
    public static final String CODE = "code";
    public static final String REDIRECT_URI_UNDEFINED = "urn:ietf:wg:oauth:2.0:oob";

    /**
     *  Parameter {@code grant_type} used in the access token request.
     */
    public static enum GrantType {
        /**
         * Used to request an access token in the Authorization Code Grant Flow.
         * The parameter key defined by the OAuth2 protocol is equal
         * to the name of this enum value converted to lowercase.
         */
        AUTHORIZATION_CODE,
        /**
         * Used to refresh an access token in the Authorization Code Grant Flow.
         * The parameter key defined by the OAuth2 protocol is equal
         * to the name of this enum value converted to lowercase.
         */
        REFRESH_TOKEN,
        /**
         * Used in Resource Owner Password Credential Grant.
         * The parameter key defined by the OAuth2 protocol is equal
         * to the name of this enum value converted to lowercase.
         */
        PASSWORD,
        /**
         * Used in Client Credentials Flow.
         * The parameter key defined by the OAuth2 protocol is equal
         * to the name of this enum value converted to lowercase.
         */
        CLIENT_CREDENTIALS;

        /**
         * Parameter key name.
         */
        public static final String key = "grant_type";
    }

    /**
     * Prevent instantiation.
     */
    private OAuth2Parameters() {
    }
}
