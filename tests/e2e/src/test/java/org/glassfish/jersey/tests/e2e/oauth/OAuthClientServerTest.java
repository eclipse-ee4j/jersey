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

package org.glassfish.jersey.tests.e2e.oauth;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import javax.inject.Inject;

import org.glassfish.jersey.client.oauth1.AccessToken;
import org.glassfish.jersey.client.oauth1.ConsumerCredentials;
import org.glassfish.jersey.client.oauth1.OAuth1AuthorizationFlow;
import org.glassfish.jersey.client.oauth1.OAuth1ClientSupport;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.oauth1.DefaultOAuth1Provider;
import org.glassfish.jersey.server.oauth1.OAuth1Provider;
import org.glassfish.jersey.server.oauth1.OAuth1ServerFeature;
import org.glassfish.jersey.server.oauth1.OAuth1ServerProperties;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.sun.security.auth.UserPrincipal;

/**
 * Tests client and server OAuth 1 functionality.
 *
 * @author Miroslav Fuksa
 */
public class OAuthClientServerTest extends JerseyTest {

    private static final String SECRET_CONSUMER_KEY = "secret-consumer-key";
    private static final String CONSUMER_KEY = "my-consumer-key";
    private static final String CONSUMER_NAME = "my-consumer";
    private static final String PROMETHEUS_TOKEN = "prometheus-token";
    private static final String PROMETHEUS_SECRET = "prometheus-secret";

    @Override
    protected Application configure() {

        final DefaultOAuth1Provider oAuthProvider = new DefaultOAuth1Provider();
        oAuthProvider.registerConsumer(CONSUMER_NAME, CONSUMER_KEY,
                SECRET_CONSUMER_KEY, new MultivaluedHashMap<String, String>());

        final Principal prometheusPrincipal = new Principal() {
            @Override
            public String getName() {
                return "prometheus";
            }
        };

        oAuthProvider.addAccessToken(PROMETHEUS_TOKEN, PROMETHEUS_SECRET, CONSUMER_KEY,
                "http://callback.url", prometheusPrincipal,
                Arrays.asList("admin", "user").stream().collect(Collectors.toSet()),
                new MultivaluedHashMap<String, String>());
        final OAuth1ServerFeature oAuth1ServerFeature = new OAuth1ServerFeature(oAuthProvider,
                "requestTokenSpecialUri", "accessTokenSpecialUri");
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(oAuth1ServerFeature);
        resourceConfig.register(MyProtectedResource.class);
        resourceConfig.register(new LoggingFeature(Logger.getLogger(OAuthClientServerTest.class.getName()),
                LoggingFeature.Verbosity.PAYLOAD_ANY));
        resourceConfig.register(OAuthAuthorizationResource.class);
        resourceConfig.property(OAuth1ServerProperties.TIMESTAMP_UNIT, "SECONDS");
        resourceConfig.property(OAuth1ServerProperties.MAX_NONCE_CACHE_SIZE, 20);
        return resourceConfig;
    }

    @Path("resource")
    public static class MyProtectedResource {

        @Context
        private SecurityContext securityContext;

        @GET
        public String get() {
            return securityContext.getUserPrincipal().getName();
        }

        @Path("admin")
        @GET
        public boolean getFoo() {
            return securityContext.isUserInRole("admin");
        }
    }

    @Path("user-authorization")
    public static class OAuthAuthorizationResource {

        @Inject
        private OAuth1Provider provider;

        @GET
        public String mustBeGetMethod(@QueryParam("oauth_token") String token) {
            System.out.println("Token received from user: " + token);
            final DefaultOAuth1Provider defProvider = (DefaultOAuth1Provider) provider;
            assertEquals("http://consumer/callback/homer", defProvider.getRequestToken(token).getCallbackUrl());

            return defProvider.authorizeToken(
                    defProvider.getRequestToken(token),
                    new UserPrincipal("homer"),
                    Collections.singleton("user"));
        }
    }

    /**
     * Tests client and server OAuth.
     * <p/>
     * Tests authorization flow including the request to a protected resource. The test uses {@link OAuth1AuthorizationFlow}
     * to perform user authorization and uses authorized client for requesting protected resource.
     * <p/>
     * The resource {@link OAuthAuthorizationResource} is used to perform user authorization (this is done
     * programmatically from the test). Finally, the Access Token is retrieved and used to request the
     * protected resource. In this resource the user principal is used to return the name of the user stored
     * in {@link SecurityContext}.
     */
    @Test
    public void testAuthorizationFlow() {
        String tempCredUri = UriBuilder.fromUri(getBaseUri()).path("requestTokenSpecialUri").build().toString();
        String accessTokenUri = UriBuilder.fromUri(getBaseUri()).path("accessTokenSpecialUri").build().toString();
        final String userAuthorizationUri = UriBuilder.fromUri(getBaseUri()).path("user-authorization").build().toString();
        final OAuth1AuthorizationFlow authFlow = OAuth1ClientSupport
                .builder(new ConsumerCredentials(CONSUMER_KEY, SECRET_CONSUMER_KEY))
                .authorizationFlow(tempCredUri, accessTokenUri, userAuthorizationUri)
                .callbackUri("http://consumer/callback/homer").build();

        final String authUri = authFlow.start();
        // authorize by a request to authorization URI
        final Response userAuthResponse = ClientBuilder.newClient().target(authUri).request().get();
        assertEquals(200, userAuthResponse.getStatus());
        final String verifier = userAuthResponse.readEntity(String.class);
        System.out.println("Verifier: " + verifier);

        authFlow.finish(verifier);
        final Client authorizedClient = authFlow.getAuthorizedClient();

        Response response = authorizedClient.target(getBaseUri()).path("resource")
                .request().get();
        assertEquals(200, response.getStatus());
        assertEquals("homer", response.readEntity(String.class));

        response = authorizedClient.target(getBaseUri()).path("resource").path("admin").request().get();
        assertEquals(200, response.getStatus());
        assertEquals(false, response.readEntity(boolean.class));
    }

    /**
     * Tests {@link org.glassfish.jersey.client.oauth1.OAuth1ClientFilter} already configured with Access Token for signature
     * purposes only.
     */
    @Test
    public void testRequestSigning() {
        final Feature filterFeature = OAuth1ClientSupport.builder(
                new ConsumerCredentials(CONSUMER_KEY, SECRET_CONSUMER_KEY)).feature()
                .accessToken(new AccessToken(PROMETHEUS_TOKEN, PROMETHEUS_SECRET)).build();
        final Client client = ClientBuilder.newBuilder()
                .register(filterFeature).build();
        final URI resourceUri = UriBuilder.fromUri(getBaseUri()).path("resource").build();
        final WebTarget target = client.target(resourceUri);
        Response response;
        for (int i = 0; i < 15; i++) {
            System.out.println("request: " + i);
            response = target.request().get();
            assertEquals(200, response.getStatus());
            assertEquals("prometheus", response.readEntity(String.class));
            i++;
            response = target.path("admin").request().get();
            assertEquals(200, response.getStatus());
            assertEquals(true, response.readEntity(boolean.class));
        }
    }

    /**
     * Tests configuration of the nonce cache on the server side.
     */
    @Test
    public void testRequestSigningWithExceedingCache() {
        final Feature filterFeature = OAuth1ClientSupport.builder(
                new ConsumerCredentials(CONSUMER_KEY, SECRET_CONSUMER_KEY)).feature()
                .accessToken(new AccessToken(PROMETHEUS_TOKEN, PROMETHEUS_SECRET)).build();
        final Client client = ClientBuilder.newBuilder()
                .register(filterFeature).build();
        final URI resourceUri = UriBuilder.fromUri(getBaseUri()).path("resource").build();
        final WebTarget target = client.target(resourceUri);
        Response response;
        for (int i = 0; i < 20; i++) {
            System.out.println("request: " + i);
            response = target.request().get();
            assertEquals(200, response.getStatus());
            assertEquals("prometheus", response.readEntity(String.class));
            i++;
            response = target.path("admin").request().get();
            assertEquals(200, response.getStatus());
            assertEquals(true, response.readEntity(boolean.class));
        }
        // now the nonce cache is full
        response = target.request().get();
        assertEquals(401, response.getStatus());
    }
}
