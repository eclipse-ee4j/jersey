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

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * OAuth2 client filter feature registers the support for performing authenticated requests to the
 * Service Provider. The feature does not perform Authorization Flow (see {@link OAuth2CodeGrantFlow}
 * for details how to use Authorization Flow and retrieve Access Token). The feature uses access to initialize
 * the internal {@link javax.ws.rs.container.ContainerRequestFilter filter}
 * which will add {@code Authorization} http header containing OAuth 2 authorization information including (based
 * on {@code bearer} tokens).
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
class OAuth2ClientFeature implements Feature {

    private final OAuth2ClientFilter filter;

    /**
     * Create a new feature initialized for the access token.
     *
     * @param accessToken Access token.
     */
    public OAuth2ClientFeature(String accessToken) {
        this.filter = new OAuth2ClientFilter(accessToken);
    }

    /**
     * Create a new filter feature with no default access token. The token will have to be
     * specified by {@link OAuth2ClientSupport#OAUTH2_PROPERTY_ACCESS_TOKEN}
     * for each request otherwise no {@code Authorization}
     * http header will be added.
     */
    public OAuth2ClientFeature() {
        this.filter = new OAuth2ClientFilter();
    }

    /**
     * Create a new feature with the given {@code filter}.
     *
     * @param filter Filter instance.
     */
    OAuth2ClientFeature(OAuth2ClientFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(filter);
        return true;
    }
}
