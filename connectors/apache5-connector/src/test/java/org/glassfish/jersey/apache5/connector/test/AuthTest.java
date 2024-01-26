/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import jakarta.inject.Singleton;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5HttpClientBuilderConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.authentication.ResponseAuthenticationException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class AuthTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(PreemptiveAuthResource.class, AuthResource.class);
    }

    @Path("/")
    public static class PreemptiveAuthResource {

        @GET
        public String get(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return e;
        }
    }

    @Test
    public void testPreemptiveAuth() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider)
                .property(Apache5ClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testPreemptiveAuthPost() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider)
                .property(Apache5ClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));
    }

    @Path("/test")
    @Singleton
    public static class AuthResource {

        int requestCount = 0;
        int queryParamsBasicRequestCount = 0;
        int queryParamsDigestRequestCount = 0;

        @GET
        public String get(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @GET
        @Path("filter")
        public String getFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return "GET";
        }

        @GET
        @Path("basicAndDigest")
        public String getBasicAndDigest(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .header("WWW-Authenticate", "Digest realm=\"WallyWorld\"")
                            .entity("Forbidden").build());
            } else if (value.startsWith("Basic")) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .header("WWW-Authenticate", "Digest realm=\"WallyWorld\"")
                            .entity("Digest authentication expected").build());
            }

            return "GET";
        }

        @GET
        @Path("noauth")
        public String get() {
            return "GET";
        }

        @GET
        @Path("digest")
        public String getDigest(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Digest realm=\"WallyWorld\"")
                            .entity("Forbidden").build());
            }

            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return e;
        }

        @POST
        @Path("filter")
        public String postFilter(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }

        @DELETE
        public void delete(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }
        }

        @DELETE
        @Path("filter")
        public void deleteFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }
        }

        @DELETE
        @Path("filter/withEntity")
        public String deleteFilterWithEntity(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }

        @GET
        @Path("content")
        public String getWithContent(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .entity("Forbidden").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @GET
        @Path("contentDigestAuth")
        public String getWithContentDigestAuth(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Digest nonce=\"1234\"")
                            .entity("Forbidden").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @GET
        @Path("queryParamsBasic")
        public String getQueryParamsBasic(@Context HttpHeaders h, @Context UriInfo uriDetails) {
            queryParamsBasicRequestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }
            return "GET " + queryParamsBasicRequestCount;
        }

        @GET
        @Path("queryParamsDigest")
        public String getQueryParamsDigest(@Context HttpHeaders h, @Context UriInfo uriDetails) {
            queryParamsDigestRequestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(
                        Response.status(401).header("WWW-Authenticate", "Digest realm=\"WallyWorld\"").build());
            }
            return "GET " + queryParamsDigestRequestCount;
        }
    }

    @Test
    public void testAuthGet() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testAuthGetWithRequestCredentialsProvider() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("GET",
                     r.request()
                      .property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider)
                      .get(String.class));
    }

    @Test
    public void testAuthGetWithClientFilter() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.basic("name", "password"));
        WebTarget r = client.target(getBaseUri()).path("test/filter");

        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testAuthGetWithBasicAndDigestFilter() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universal("name", "password"));
        WebTarget r = client.target(getBaseUri()).path("test/basicAndDigest");

        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testAuthGetBasicNoChallenge() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.basicBuilder().build());
        WebTarget r = client.target(getBaseUri()).path("test/noauth");

        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testAuthGetWithDigestFilter() {
        ClientConfig cc = new ClientConfig();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cc.connectorProvider(new Apache5ConnectorProvider());
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, cm);
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universal("name", "password"));
        WebTarget r = client.target(getBaseUri()).path("test/digest");

        assertEquals("GET", r.request().get(String.class));

        // Verify the connection that was used for the request is available for reuse
        // and no connections are leased
        assertEquals(cm.getTotalStats().getAvailable(), 1);
        assertEquals(cm.getTotalStats().getLeased(), 0);
    }

    @Test
    @Disabled("JERSEY-1750: Cannot retry request with a non-repeatable request entity. How to buffer the entity?"
            + " Allow repeatable write in jersey?")
    public void testAuthPost() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));
    }

    @Test
    public void testAuthPostWithClientFilter() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.basic("name", "password"));
        WebTarget r = client.target(getBaseUri()).path("test/filter");

        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));
    }

    @Test
    public void testAuthDelete() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );
        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        Response response = r.request().delete();
        assertEquals(response.getStatus(), 204);
    }

    @Test
    public void testAuthDeleteWithClientFilter() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.basic("name", "password"));
        WebTarget r = client.target(getBaseUri()).path("test/filter");

        Response response = r.request().delete();
        assertEquals(204, response.getStatus());
    }

    @Test
    public void testAuthInteractiveGet() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );
        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    @Disabled("JERSEY-1750: Cannot retry request with a non-repeatable request entity. How to buffer the entity?"
            + " Allow repeatable write in jersey?")
    public void testAuthInteractivePost() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );

        ClientConfig cc = new ClientConfig();
        cc.property(Apache5ClientProperties.CREDENTIALS_PROVIDER, credentialsProvider);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("POST", r.request().post(Entity.text("POST"), String.class));
    }

    @Test
    public void testAuthGetWithBasicFilterAndContent() {
        ClientConfig cc = new ClientConfig();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cc.connectorProvider(new Apache5ConnectorProvider());
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, cm);
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universalBuilder().build());
        WebTarget r = client.target(getBaseUri()).path("test/content");

        try {
            assertEquals("GET", r.request().get(String.class));
            fail();
        } catch (ResponseAuthenticationException ex) {
            // expected
        }

        // Verify the connection that was used for the request is available for reuse
        // and no connections are leased
        assertEquals(cm.getTotalStats().getAvailable(), 1);
        assertEquals(cm.getTotalStats().getLeased(), 0);
    }

    @Test
    public void testAuthGetWithDigestFilterAndContent() {
        ClientConfig cc = new ClientConfig();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cc.connectorProvider(new Apache5ConnectorProvider());
        cc.property(Apache5ClientProperties.CONNECTION_MANAGER, cm);
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universalBuilder().build());
        WebTarget r = client.target(getBaseUri()).path("test/contentDigestAuth");

        try {
            assertEquals("GET", r.request().get(String.class));
            fail();
        } catch (ResponseAuthenticationException ex) {
            // expected
        }

        // Verify the connection that was used for the request is available for reuse
        // and no connections are leased
        assertEquals(cm.getTotalStats().getAvailable(), 1);
        assertEquals(cm.getTotalStats().getLeased(), 0);
    }

    @Test
    public void testAuthGetQueryParamsBasic() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universal("name", "password"));

        WebTarget r = client.target(getBaseUri()).path("test/queryParamsBasic");
        assertEquals("GET 2", r.request().get(String.class));

        r = client.target(getBaseUri())
                .path("test/queryParamsBasic")
                .queryParam("param1", "value1")
                .queryParam("param2", "value2");
        assertEquals("GET 3", r.request().get(String.class));

    }

    @Test
    public void testAuthGetQueryParamsDigest() {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        client.register(HttpAuthenticationFeature.universal("name", "password"));

        WebTarget r = client.target(getBaseUri()).path("test/queryParamsDigest");
        assertEquals("GET 2", r.request().get(String.class));

        r = client.target(getBaseUri())
                .path("test/queryParamsDigest")
                .queryParam("param1", "value1")
                .queryParam("param2", "value2");
        assertEquals("GET 3", r.request().get(String.class));
    }

    @Test
    public void testAuthGetWithConfigurator() {
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope("localhost", getPort()),
                new UsernamePasswordCredentials("name", "password".toCharArray())
        );
        Apache5HttpClientBuilderConfigurator apache5HttpClientBuilderConfigurator = (httpClientBuilder) -> {
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        };

        ClientConfig cc = new ClientConfig();
        cc.register(apache5HttpClientBuilderConfigurator);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri()).path("test");

        assertEquals("GET", r.request().get(String.class));
    }
}
