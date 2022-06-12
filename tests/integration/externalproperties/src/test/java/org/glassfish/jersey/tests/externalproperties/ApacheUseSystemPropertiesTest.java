/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.jersey.ExternalProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

public class ApacheUseSystemPropertiesTest extends JerseyTest {

    private SSLContext serverSslContext;
    private SSLParameters serverSslParameters;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @Path("secure")
    public static class TestResource {

        @GET
        public Response getOk() {
            return Response.ok("ok").build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder
                .fromUri("https://localhost")
                .port(getPort())
                .build();
    }

    @Override
    protected Optional<SSLContext> getSslContext() {
        if (serverSslContext == null) {
            serverSslContext = SslUtils.createServerSslContext();
        }
        return Optional.of(serverSslContext);
    }

    @Override
    protected Optional<SSLParameters> getSslParameters() {
        if (serverSslParameters == null) {
            serverSslParameters = new SSLParameters();
            serverSslParameters.setNeedClientAuth(false);
        }
        return Optional.of(serverSslParameters);
    }

    @After
    public void cleanSystemProperties() {
        System.getProperties().remove(ExternalProperties.HTTPS_PROTOCOLS);
        System.getProperties().remove(ExternalProperties.HTTPS_CIPHERSUITES);
    }

    private ClientConfig createApacheConfig() {
        ClientConfig config = new ClientConfig();
        config.property(ApacheClientProperties.USE_SYSTEM_PROPERTIES, true);
        config.connectorProvider(new ApacheConnectorProvider());
        return config;
    }

    @Test
    public void testAllowedProtocol() {
        System.setProperty(ExternalProperties.HTTPS_PROTOCOLS, "TLSv1.2");
        SSLContext clientSslContext = SslUtils.createClientSslContext("TLSv1.2");

        Client client = ClientBuilder.newBuilder()
                .withConfig(createApacheConfig())
                .sslContext(clientSslContext)
                .build();

        Response response = client.target(getBaseUri()).path("secure").request().get();
        Assert.assertEquals("ok", response.readEntity(String.class));
    }

    @Test
    public void testNotAllowedProtocol() {
        System.setProperty(ExternalProperties.HTTPS_PROTOCOLS, "SSLv3");
        SSLContext clientSslContext = SslUtils.createClientSslContext("TLSv1.2");

        Client client = ClientBuilder.newBuilder()
                .withConfig(createApacheConfig())
                .sslContext(clientSslContext)
                .build();

        exception.expect(ProcessingException.class);
        client.target(getBaseUri()).path("secure").request().get();
    }

    @Test
    public void testAllowedCipherSuites() {
        System.setProperty(ExternalProperties.HTTPS_CIPHERSUITES, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        System.setProperty(ExternalProperties.HTTPS_PROTOCOLS, "TLSv1.2");
        SSLContext clientSslContext = SslUtils.createClientSslContext("TLSv1.2");

        Client client = ClientBuilder.newBuilder()
                .withConfig(createApacheConfig())
                .sslContext(clientSslContext)
                .build();

        Response response = client.target(getBaseUri()).path("secure").request().get();
        Assert.assertEquals("ok", response.readEntity(String.class));
    }

    @Test
    public void testNotAllowedCipherSuites() {
        System.setProperty(ExternalProperties.HTTPS_CIPHERSUITES, "TLS_AES");
        System.setProperty(ExternalProperties.HTTPS_PROTOCOLS, "TLSv1.2");
        SSLContext clientSslContext = SslUtils.createClientSslContext("TLSv1.2");

        Client client = ClientBuilder.newBuilder()
                .withConfig(createApacheConfig())
                .sslContext(clientSslContext)
                .build();

        exception.expect(ProcessingException.class);
        client.target(getBaseUri()).path("secure").request().get();
    }

}
