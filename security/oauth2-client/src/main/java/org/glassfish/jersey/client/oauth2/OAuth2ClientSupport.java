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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;

/**
 * Main class to build the Authorization Flow instances and {@link javax.ws.rs.core.Feature client filter feature} that
 * can supports performing of authenticated OAuth requests.
 * <p><b>Authorization flow</b></p>
 * For more information about authorization flow, see {@link OAuth2CodeGrantFlow}.
 * <p><b>Client feature</b></p>
 * <p>
 * Use method {@link #feature(String)} to build the feature. OAuth2 client filter feature registers
 * the support for performing authenticated requests to the
 * Service Provider. The feature uses an access token to initialize
 * the internal {@link javax.ws.rs.container.ContainerRequestFilter filter}
 * which will add {@code Authorization} http header containing OAuth 2 authorization information (based
 * on {@code bearer} tokens).
 * </p>
 *
 * <p>
 * The internal filter can be controlled by properties put into
 * the {@link javax.ws.rs.client.ClientRequestContext client request}
 * using {@link javax.ws.rs.client.ClientRequestContext#setProperty(String, Object)} method. The property key
 * is defined in this class as a static variables
 * ({@link OAuth2ClientSupport#OAUTH2_PROPERTY_ACCESS_TOKEN} (see its javadoc for usage).
 * Using the property a specific
 * access token can be defined for each request.
 * </p>
 * Example of using specific access token for one request:
 * <pre>
 * final Response response = client.target("foo").request()
 *           .property(OAUTH2_PROPERTY_ACCESS_TOKEN, "6ab45ab465e46f54d771a").get();
 * </pre>

 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public final class OAuth2ClientSupport {
    /**
     * Key of the property that can be attached to the
     * {@link javax.ws.rs.client.ClientRequestContext client request} using
     * {@link javax.ws.rs.client.ClientRequestContext#setProperty(String, Object)} and that
     * defines access token that should be used when generating OAuth {@code Authorization}
     * http header. The property will override the setting of the internal
     * {@link javax.ws.rs.client.ClientRequestFilter filter} for the current request only. This property
     * can be used only when {@link javax.ws.rs.core.Feature OAauth 2 filter feature} is
     * registered into the {@link javax.ws.rs.client.Client}
     * instance.
     * <p>
     * The value of the property must be a {@link String}.
     * </p>
     */
    public static final String OAUTH2_PROPERTY_ACCESS_TOKEN = "jersey.config.client.oauth2.access.token";

    /**
     * Build the {@link Feature client filter feature} from the {@code accessToken} that will add
     * {@code Authorization} http header to the request with the OAuth authorization information.
     *
     *
     * @param accessToken Access token to be used in the authorization header or {@code null}
     *                    if no default access token should be defined. In this case the token
     *                    will have to be set for each request using {@link #OAUTH2_PROPERTY_ACCESS_TOKEN}
     *                    property.
     * @return Client feature.
     */
    public static Feature feature(String accessToken) {
        return new OAuth2ClientFeature(accessToken);
    }

    /**
     * Get the builder of the {@link OAuth2CodeGrantFlow Authorization Code Grant Flow}.
     *
     * @param clientIdentifier Client identifier (id of application that wants to be approved). Issued by the
     *                         Service Provider.
     * @param authorizationUri The URI to which the user should be redirected to authorize our application.
     *                         The URI points to the
     *                          authorization server and is defined by the Service Provider.
     * @param accessTokenUri The access token URI on which the access token can be requested. The URI points to the
     *                          authorization server and is defined by the Service Provider.
     * @return builder of the {@link OAuth2CodeGrantFlow Authorization Code Grant Flow}.
     */
    public static OAuth2CodeGrantFlow.Builder authorizationCodeGrantFlowBuilder(ClientIdentifier clientIdentifier,
                                                                                String authorizationUri,
                                                                                String accessTokenUri) {
        return new AuthCodeGrantImpl.Builder(clientIdentifier, authorizationUri, accessTokenUri);
    }

    /**
     * Get a builder that can be directly used to perform Authorization Code Grant flow defined by
     * Google.
     *
     * @param clientIdentifier Client identifier (id of application that wants to be approved). Issued by the
     *                         Service Provider.
     * @param redirectURI URI to which the user (resource owner) should be redirected after he/she
     *                    grants access to our application or {@code null} if the application
     *                    does not support redirection (eg. is not a web server).
     * @param scope The api to which an access is requested (eg. Google tasks).
     * @return Google builder instance.
     */
    public static OAuth2FlowGoogleBuilder googleFlowBuilder(ClientIdentifier clientIdentifier,
                                                            String redirectURI, String scope) {

        return new OAuth2FlowGoogleBuilder()
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .accessTokenUri("https://accounts.google.com/o/oauth2/token")
                .redirectUri(redirectURI == null ? OAuth2Parameters.REDIRECT_URI_UNDEFINED : redirectURI)
                .clientIdentifier(clientIdentifier)
                .scope(scope);
    }

    /**
     * Get a builder that can be directly used to perform Authorization Code Grant flow defined by
     * Facebook.
     *
     * @param clientIdentifier Client identifier (id of application that wants to be approved). Issued by the
     *                         Service Provider.
     * @param redirectURI URI to which the user (resource owner) should be redirected after he/she
     *                    grants access to our application or {@code null} if the application
     *                    does not support redirection (eg. is not a web server).
     * </p>
     * @return Builder instance.
     */
    public static OAuth2CodeGrantFlow.Builder facebookFlowBuilder(ClientIdentifier clientIdentifier,
                                                                  String redirectURI) {
        return OAuth2FlowFacebookBuilder.getFacebookAuthorizationBuilder(clientIdentifier, redirectURI,
                ClientBuilder.newClient());
    }

    /**
     * Prevent instantiation.
     */
    private OAuth2ClientSupport() {
    }
}

