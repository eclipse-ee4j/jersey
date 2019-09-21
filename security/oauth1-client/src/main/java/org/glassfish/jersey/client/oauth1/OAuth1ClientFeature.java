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
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;
import org.glassfish.jersey.oauth1.signature.OAuth1SignatureFeature;

/**
 * OAuth1 client filter feature registers the support for performing authenticated requests to the
 * Service Provider. The feature does not perform Authorization Flow (see {@link OAuth1AuthorizationFlow}
 * for details how to use Authorization Flow and retrieve Access Token). The feature uses {@link ConsumerCredentials}
 * and {@link AccessToken} to initialize the internal {@link javax.ws.rs.container.ContainerRequestFilter filter}
 * which will add {@code Authorization} headers containing OAuth authorization information including
 * the oauth signature.
 * <p>
 * The internal filter can be controlled by properties put into
 * the {@link javax.ws.rs.client.ClientRequestContext client request}
 * using {@link javax.ws.rs.client.ClientRequestContext#setProperty(String, Object)} method. The property keys
 * are defined in this class as a static variables (see their javadocs for usage). Using these properties a specific
 * {@link AccessToken} can be defined for each request for example.
 * </p>
 * Example of using specific access token for one request:
 * <pre>
 * final Response response = client.target("foo").request()
 *           .property(OAUTH_PROPERTY_ACCESS_TOKEN, new AccessToken("ab454f84e", "f454de848a54b")).get();
 * </pre>
 * <p>
 * See {@link OAuth1Builder} for more information of how to build this feature.
 * </p>
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
final class OAuth1ClientFeature implements Feature {

    private final OAuth1Parameters parameters;
    private final OAuth1Secrets secrets;

    /**
     * Create a new feature.
     *
     * @param parameters OAuth parameters.
     * @param secrets OAuth client/token secret.
     */
    OAuth1ClientFeature(final OAuth1Parameters parameters, final OAuth1Secrets secrets) {
        this.parameters = parameters;
        this.secrets = secrets;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(OAuth1SignatureFeature.class);
        context.register(OAuth1ClientFilter.class);

        context.property(OAuth1ClientSupport.OAUTH_PROPERTY_OAUTH_PARAMETERS, parameters);
        context.property(OAuth1ClientSupport.OAUTH_PROPERTY_OAUTH_SECRETS, secrets);

        return true;
    }
}
