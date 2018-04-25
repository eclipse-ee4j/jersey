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

import java.util.logging.Logger;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.oauth1.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;

/**
 * Default implementation of {@link OAuth1AuthorizationFlow}. The instance is used
 * to perform authorization flows.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
class OAuth1AuthorizationFlowImpl implements OAuth1AuthorizationFlow {

    private static final Logger LOGGER = Logger.getLogger(OAuth1AuthorizationFlowImpl.class.getName());

    /**
     * OAuth1AuthorizationFlowImpl builder.
     */
    static class Builder implements OAuth1Builder.FlowBuilder {

        private final OAuth1Parameters params;
        private final OAuth1Secrets secrets;

        private String requestTokenUri;
        private String accessTokenUri;
        private String authorizationUri;
        private Client client;
        private String callbackUri;

        private boolean enableLogging;

        /**
         * Create a new builder.
         * @param params Pre-configured oauth parameters.
         * @param secrets Pre-configured oauth secrets.
         * @param requestTokenUri Request token uri.
         * @param accessTokenUri Access token uri.
         * @param authorizationUri Authorization uri.
         */
        public Builder(final OAuth1Parameters params,
                       final OAuth1Secrets secrets,
                       final String requestTokenUri,
                       final String accessTokenUri,
                       final String authorizationUri) {
            this.params = params;
            this.secrets = secrets;

            this.requestTokenUri = requestTokenUri;
            this.accessTokenUri = accessTokenUri;
            this.authorizationUri = authorizationUri;
        }

        @Override
        public OAuth1Builder.FlowBuilder callbackUri(final String callbackUri) {
            this.callbackUri = callbackUri;
            return this;
        }

        @Override
        public OAuth1Builder.FlowBuilder client(final Client client) {
            this.client = client;
            return this;
        }

        @Override
        public OAuth1Builder.FlowBuilder enableLogging() {
            this.enableLogging = true;
            return this;
        }

        public OAuth1AuthorizationFlowImpl build() {
            return new OAuth1AuthorizationFlowImpl(params, secrets, requestTokenUri, accessTokenUri, authorizationUri,
                    callbackUri, client, enableLogging);
        }
    }

    /** The OAuth parameters to be used in generating signature. */
    private final OAuth1Parameters parameters;

    /** The OAuth secrets to be used in generating signature. */
    private final OAuth1Secrets secrets;

    private final String requestTokenUri;
    private final String accessTokenUri;
    private final String authorizationUri;

    private final Client client;

    private volatile AccessToken accessToken;
    private final Value<Feature> oAuth1ClientFilterFeature = new Value<Feature>() {
        @Override
        public Feature get() {
            return new OAuth1BuilderImpl(parameters, secrets).feature()
                    .accessToken(accessToken)
                    .build();
        }
    };

    private OAuth1AuthorizationFlowImpl(final OAuth1Parameters params, final OAuth1Secrets secrets, final String requestTokenUri,
                                        final String accessTokenUri, final String authorizationUri, final String callbackUri,
                                        final Client client, final boolean enableLogging) {
        this.parameters = params;
        this.secrets = secrets;
        this.requestTokenUri = requestTokenUri;
        this.accessTokenUri = accessTokenUri;
        this.authorizationUri = authorizationUri;

        if (client != null) {
            this.client = client;
        } else {
            this.client = ClientBuilder.newBuilder().build();
        }

        final Configuration config = this.client.getConfiguration();

        if (enableLogging && !config.isRegistered(LoggingFeature.class)) {
            this.client.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        }
        if (!config.isRegistered(OAuth1ClientFeature.class)) {
            this.client.register(new OAuth1ClientFeature(params, secrets));
        }

        if (callbackUri != null) {
            this.parameters.callback(callbackUri);
        }

        if (secrets.getConsumerSecret() == null || parameters.getConsumerKey() == null) {
            throw new IllegalStateException(LocalizationMessages.ERROR_CONFIGURATION_MISSING_CONSUMER());
        }
    }

    private Invocation.Builder addProperties(final Invocation.Builder invocationBuilder) {
        return invocationBuilder
                .property(OAuth1ClientSupport.OAUTH_PROPERTY_OAUTH_PARAMETERS, parameters)
                .property(OAuth1ClientSupport.OAUTH_PROPERTY_OAUTH_SECRETS, secrets);
    }

    public String start() {
        final Response response = addProperties(client.target(requestTokenUri).request())
                .post(null);
        if (response.getStatus() != 200) {
            throw new RuntimeException(LocalizationMessages.ERROR_REQUEST_REQUEST_TOKEN(response.getStatus()));
        }
        final MultivaluedMap<String, String> formParams = response.readEntity(Form.class).asMap();
        parameters.token(formParams.getFirst(OAuth1Parameters.TOKEN));
        secrets.tokenSecret(formParams.getFirst(OAuth1Parameters.TOKEN_SECRET));

        return UriBuilder.fromUri(authorizationUri).queryParam(OAuth1Parameters.TOKEN, parameters.getToken())
                .build().toString();
    }

    public AccessToken finish() {
        return finish(null);
    }

    public AccessToken finish(final String verifier) {
        parameters.setVerifier(verifier);
        final Response response = addProperties(client.target(accessTokenUri).request()).post(null);
        // accessToken request failed
        if (response.getStatus() >= 400) {
            throw new RuntimeException(LocalizationMessages.ERROR_REQUEST_ACCESS_TOKEN(response.getStatus()));
        }
        final Form form = response.readEntity(Form.class);
        final String accessToken = form.asMap().getFirst(OAuth1Parameters.TOKEN);
        final String accessTokenSecret = form.asMap().getFirst(OAuth1Parameters.TOKEN_SECRET);

        if (accessToken == null) {
            throw new NotAuthorizedException(LocalizationMessages.ERROR_REQUEST_ACCESS_TOKEN_NULL());
        }

        parameters.token(accessToken);
        secrets.tokenSecret(accessTokenSecret);
        final AccessToken resultToken = new AccessToken(parameters.getToken(), secrets.getTokenSecret());
        this.accessToken = resultToken;
        return resultToken;
    }

    @Override
    public Client getAuthorizedClient() {
        return ClientBuilder.newClient().register(getOAuth1Feature());
    }

    @Override
    public Feature getOAuth1Feature() {
        if (this.accessToken == null) {
            throw new IllegalStateException(LocalizationMessages.ERROR_FLOW_NOT_FINISHED());
        }
        return oAuth1ClientFilterFeature.get();
    }
}
