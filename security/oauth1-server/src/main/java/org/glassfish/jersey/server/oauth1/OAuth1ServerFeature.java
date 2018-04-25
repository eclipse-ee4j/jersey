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

package org.glassfish.jersey.server.oauth1;

import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.oauth1.signature.OAuth1SignatureFeature;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.oauth1.internal.AccessTokenResource;
import org.glassfish.jersey.server.oauth1.internal.RequestTokenResource;

/**
 * The feature enables support for OAuth 1.0a on the server.
 * <p/>
 * The OAuth 1 server support requires implementation of {@link OAuth1Provider};
 * which will be used to retrieve Request Tokens, Access tokens, etc. The implementation should be configured
 * in this feature or registered as a standard provider.
 * <p/>
 * Feature can be created and configured by various constructors. Beside that, the feature behaviour
 * can be overwritten by configuration properties {@link OAuth1ServerProperties#ENABLE_TOKEN_RESOURCES},
 * {@link OAuth1ServerProperties#REQUEST_TOKEN_URI} and {@link OAuth1ServerProperties#ACCESS_TOKEN_URI}.
 *
 * @author Miroslav Fuksa
 */
public class OAuth1ServerFeature implements Feature {

    private final OAuth1Provider oAuth1Provider;
    private final String requestTokenUri;
    private final String accessTokenUri;

    /**
     * Create a new feature configured with {@link OAuth1Provider OAuth provider} and request and access token
     * URIs. The feature also exposes Request and Access Token Resources.
     * These resources are part of the Authorization process and
     * grant Request and Access tokens. Resources will be available on
     * URIs defined by parameters {@code requestTokenUri} and {@code accessTokenUri}.
     *
     * @param oAuth1Provider Instance of the {@code OAuth1Provider} that will handle authorization. If the value is
     *                       {@code null}, then the provider must be registered explicitly outside of this feature
     *                       as a standard provider.
     * @param requestTokenUri URI (relative to application context path) of Request Token Resource that will be exposed.
     * @param accessTokenUri URI (relative to application context path) of Request Token Resource that will be exposed.
     */
    public OAuth1ServerFeature(OAuth1Provider oAuth1Provider,
                               String requestTokenUri,
                               String accessTokenUri) {
        this.oAuth1Provider = oAuth1Provider;
        this.requestTokenUri = requestTokenUri;
        this.accessTokenUri = accessTokenUri;
    }

    /**
     * Create a new feature configured with {@link OAuth1Provider OAuth provider}. The feature will not
     * expose Request and Access Token Resources. The OAuth 1 support will not be responsible for handling
     * these authorization request types.
     *
     * @param oAuth1Provider Instance of the {@code OAuth1Provider} that will handle authorization.
     */
    public OAuth1ServerFeature(OAuth1Provider oAuth1Provider) {
        this(oAuth1Provider, null, null);
    }

    /**
     * Create a new feature. The feature will not register any {@link OAuth1Provider OAuth provider}
     * and it will not expose Request and Access Token Resources. {@code OAuth1Provider} must be registered
     * explicitly as a standard provider. As Token Resources are not exposed, the OAuth 1 support will
     * not be responsible for handling Token Requests.
     */
    public OAuth1ServerFeature() {
        this(null);
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (oAuth1Provider != null) {
            context.register(oAuth1Provider);
        }

        context.register(OAuth1ServerFilter.class);

        if (!context.getConfiguration().isRegistered(OAuth1SignatureFeature.class)) {
            context.register(OAuth1SignatureFeature.class);
        }

        final Map<String, Object> properties = context.getConfiguration().getProperties();
        final Boolean propertyResourceEnabled = OAuth1ServerProperties.getValue(properties,
                OAuth1ServerProperties.ENABLE_TOKEN_RESOURCES, null, Boolean.class);

        boolean registerResources = propertyResourceEnabled != null
                ? propertyResourceEnabled : requestTokenUri != null & accessTokenUri != null;

        if (registerResources) {
            String requestUri = OAuth1ServerProperties.getValue(properties, OAuth1ServerProperties.REQUEST_TOKEN_URI,
                    null, String.class);
            if (requestUri == null) {
                requestUri = requestTokenUri == null ? "requestToken" : requestTokenUri;
            }

            String accessUri = OAuth1ServerProperties.getValue(properties, OAuth1ServerProperties.ACCESS_TOKEN_URI,
                    null, String.class);
            if (accessUri == null) {
                accessUri = accessTokenUri == null ? "accessToken" : accessTokenUri;
            }

            final Resource requestResource = Resource.builder(RequestTokenResource.class).path(requestUri).build();
            final Resource accessResource = Resource.builder(AccessTokenResource.class).path(accessUri).build();

            context.register(new OAuthModelProcessor(requestResource, accessResource));
        }
        return true;
    }


    @Priority(100)
    private static class OAuthModelProcessor implements ModelProcessor {
        private final Resource[] resources;

        private OAuthModelProcessor(Resource... resources) {
            this.resources = resources;
        }

        @Override
        public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
            final ResourceModel.Builder builder = new ResourceModel.Builder(resourceModel, false);
            for (Resource resource : resources) {
                builder.addResource(resource);
            }

            return builder.build();
        }

        @Override
        public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
            return subResourceModel;
        }
    }
}
