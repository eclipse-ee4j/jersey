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

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.oauth2.ClientIdentifier;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;
import org.glassfish.jersey.client.oauth2.OAuth2Parameters;
import org.glassfish.jersey.client.oauth2.TokenResult;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests OAuth 2 client.
 *
 * @author Miroslav Fuksa
 */
public class OAuth2Test extends JerseyTest {

    private static final String STATE = "4564dsf54654fsda654af";
    private static final String CODE = "code-xyz";
    private static final String CLIENT_PUBLIC = "clientPublic";
    private static final String CLIENT_SECRET = "clientSecret";

    @Override
    protected Application configure() {
        return new ResourceConfig(MoxyJsonFeature.class, AuthorizationResource.class)
                .register(new LoggingFeature(Logger.getAnonymousLogger(), LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    @Path("oauth")
    public static class AuthorizationResource {

        @POST
        @Path("access-token")
        @Produces(MediaType.APPLICATION_JSON)
        public MyTokenResult getAccessToken(@FormParam("grant_type") String grantType,
                                            @FormParam("code") String code,
                                            @FormParam("redirect_uri") String redirectUri,
                                            @FormParam("client_id") String clientId) {
            try {
                assertEquals("authorization_code", grantType);
                assertEquals("urn:ietf:wg:oauth:2.0:oob", redirectUri);
                assertEquals(CODE, code);
                assertEquals(CLIENT_PUBLIC, clientId);
            } catch (AssertionError e) {
                e.printStackTrace();
                throw new BadRequestException(Response.status(400).entity(e.getMessage()).build());
            }

            final MyTokenResult myTokenResult = new MyTokenResult();
            myTokenResult.setAccessToken("access-token-aab999f");
            myTokenResult.setExpiresIn("3600");
            myTokenResult.setTokenType("access-token");
            myTokenResult.setRefreshToken("refresh-xyz");
            return myTokenResult;
        }

        @GET
        @Path("authorization")
        public String authorization(@QueryParam("state") String state,
                                    @QueryParam("response_type") String responseType,
                                    @QueryParam("scope") String scope,
                                    @QueryParam("readOnly") String readOnly,
                                    @QueryParam("redirect_uri") String redirectUri) {
            try {
                assertEquals("code", responseType);
                assertEquals(STATE, state);
                assertEquals("urn:ietf:wg:oauth:2.0:oob", redirectUri);
                assertEquals("contact", scope);
                assertEquals("true", readOnly);
            } catch (AssertionError e) {
                e.printStackTrace();
                throw new BadRequestException(Response.status(400).entity(e.getMessage()).build());
            }
            return CODE;
        }

        @POST
        @Path("refresh-token")
        @Produces(MediaType.APPLICATION_JSON)
        public String refreshToken(@FormParam("grant_type") String grantType,
                                   @FormParam("refresh_token") String refreshToken,
                                   @HeaderParam("isArray") @DefaultValue("false") boolean isArray) {
            try {
                assertEquals("refresh_token", grantType);
                assertEquals("refresh-xyz", refreshToken);
            } catch (AssertionError e) {
                e.printStackTrace();
                throw new BadRequestException(Response.status(400).entity(e.getMessage()).build());
            }

            return isArray
                    ? "{\"access_token\":[\"access-token-new\"],\"expires_in\":\"3600\",\"token_type\":\"access-token\"}"
                    : "{\"access_token\":\"access-token-new\",\"expires_in\":\"3600\",\"token_type\":\"access-token\"}";
        }
    }

    @Test
    public void testFlow() {
        testFlow(false);
    }

    @Test
    public void testFlowWithArrayInResponse() {
        testFlow(true);
    }

    private void testFlow(final boolean isArray) {
        ClientIdentifier clientId = new ClientIdentifier(CLIENT_PUBLIC, CLIENT_SECRET);
        final String authUri = UriBuilder.fromUri(getBaseUri()).path("oauth").path("authorization").build().toString();
        final String accessTokenUri = UriBuilder.fromUri(getBaseUri()).path("oauth").path("access-token").build().toString();
        final String refreshTokenUri = UriBuilder.fromUri(getBaseUri()).path("oauth").path("refresh-token").build().toString();
        final String state = STATE;

        final Client client = ClientBuilder.newClient();
        if (isArray) {
            client.register(new ClientRequestFilter() {
                @Override
                public void filter(final ClientRequestContext requestContext) throws IOException {
                    requestContext.getHeaders().putSingle("isArray", true);
                }
            });
        }

        final OAuth2CodeGrantFlow.Builder builder =
                OAuth2ClientSupport.authorizationCodeGrantFlowBuilder(clientId, authUri, accessTokenUri);
        final OAuth2CodeGrantFlow flow = builder
                .client(client)
                .refreshTokenUri(refreshTokenUri)
                .property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, "readOnly", "true")
                .property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, OAuth2Parameters.STATE, state)
                .scope("contact")
                .build();
        final String finalAuthorizationUri = flow.start();

        final Response response = ClientBuilder.newClient().target(finalAuthorizationUri).request().get();
        assertEquals(200, response.getStatus());

        final String code = response.readEntity(String.class);
        assertEquals(CODE, code);

        final TokenResult result = flow.finish(code, state);
        assertEquals("access-token-aab999f", result.getAccessToken());
        assertEquals(new Long(3600), result.getExpiresIn());
        assertEquals("access-token", result.getTokenType());

        final TokenResult refreshResult = flow.refreshAccessToken(result.getRefreshToken());
        assertEquals("access-token-new", refreshResult.getAccessToken());
        assertEquals(new Long(3600), refreshResult.getExpiresIn());
        assertEquals("access-token", refreshResult.getTokenType());

        if (isArray) {
            final Collection<String> array = (Collection<String>) refreshResult.getAllProperties().get("access_token");

            assertThat(array.size(), is(1));
            assertThat(array, hasItem("access-token-new"));
        }
    }

    @XmlRootElement
    public static class MyTokenResult {

        @XmlAttribute(name = "access_token")
        private String accessToken;
        @XmlAttribute(name = "expires_in")
        private String expiresIn;
        @XmlAttribute(name = "token_type")
        private String tokenType;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        @XmlAttribute(name = "refresh_token")
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(String expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }
}
