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

package org.glassfish.jersey.client.oauth1;

import javax.ws.rs.core.Feature;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;

/**
 * OAuth 1 client builder default implementation.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
class OAuth1BuilderImpl implements OAuth1Builder {

    private final OAuth1Parameters params;
    private final OAuth1Secrets secrets;
    private ConsumerCredentials consumerCredentials;

    /**
     * Create a new builder instance.
     *
     * @param consumerCredentials Consumer credentials.
     */
    OAuth1BuilderImpl(final ConsumerCredentials consumerCredentials) {
        this(new OAuth1Parameters(), new OAuth1Secrets(), consumerCredentials);
    }

    /**
     * Create a new builder instance.
     *
     * @param params Pre-configured oauth parameters.
     * @param secrets Pre-configured oauth secrets.
     */
    OAuth1BuilderImpl(final OAuth1Parameters params, final OAuth1Secrets secrets) {
        this(params, secrets, new ConsumerCredentials(params.getConsumerKey(), secrets.getConsumerSecret()));
    }

    private OAuth1BuilderImpl(final OAuth1Parameters params, final OAuth1Secrets secrets,
                              final ConsumerCredentials consumerCredentials) {
        this.params = params;
        this.secrets = secrets;

        // spec defines that when no callback uri is used (e.g. client is unable to receive callback
        // as it is a mobile application), the "oob" value should be used.
        if (this.params.getCallback() == null) {
            this.params.setCallback(OAuth1Parameters.NO_CALLBACK_URI_VALUE);
        }
        this.consumerCredentials = consumerCredentials;
    }

    @Override
    public OAuth1BuilderImpl signatureMethod(String signatureMethod) {
        params.setSignatureMethod(signatureMethod);
        return this;
    }

    @Override
    public OAuth1BuilderImpl realm(String realm) {
        params.setRealm(realm);
        return this;
    }

    @Override
    public OAuth1BuilderImpl timestamp(String timestamp) {
        params.setTimestamp(timestamp);
        return this;
    }

    @Override
    public OAuth1BuilderImpl nonce(String timestamp) {
        params.setNonce(timestamp);
        return this;
    }

    @Override
    public OAuth1BuilderImpl version(String timestamp) {
        params.setVersion(timestamp);
        return this;
    }

    @Override
    public FilterFeatureBuilder feature() {
        defineCredentialsParams();
        return new FilterBuilderImpl(params, secrets);
    }

    private void defineCredentialsParams() {
        params.setConsumerKey(consumerCredentials.getConsumerKey());
        secrets.setConsumerSecret(consumerCredentials.getConsumerSecret());
    }

    @Override
    public FlowBuilder authorizationFlow(String requestTokenUri, String accessTokenUri, String authorizationUri) {
        defineCredentialsParams();
        return new OAuth1AuthorizationFlowImpl.Builder(params, secrets, requestTokenUri, accessTokenUri, authorizationUri);
    }

    /**
     * OAuth 1 client filter feature builder default implementation.
     */
    static class FilterBuilderImpl implements FilterFeatureBuilder {

        private final OAuth1Parameters params;
        private final OAuth1Secrets secrets;
        private AccessToken accessToken;

        /**
         * Create a new builder instance.
         *
         * @param params Pre-configured oauth parameters.
         * @param secrets Pre-configured oauth secrets.
         */
        FilterBuilderImpl(OAuth1Parameters params, OAuth1Secrets secrets) {
            this.params = params;
            this.secrets = secrets;
        }

        @Override
        public FilterFeatureBuilder accessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        @Override
        public Feature build() {
            if (accessToken != null) {
                params.setToken(accessToken.getToken());
                secrets.setTokenSecret(accessToken.getAccessTokenSecret());
            }
            return new OAuth1ClientFeature(params, secrets);
        }
    }
}
