/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.grizzly.web.ssl;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

/**
 * Basic GrizzlyWebTestContainerFactory unit tests.
 *
 * The test contains a server configured with SSL at https://localhost:9998/
 * With two way authentication the client will validate the identity of the server with
 * the trusted certificates within the trust store of the client. The server will also validate
 * the identity of the client with the trusted certificates within the trust store of the server.
 * If they trust each other the ssl handshake process will proceed or else the request will fail
 *
 * @author Hakan Altindag
 */
public class GrizzlyTwoWaySslWebTest extends JerseyTest {

    private SSLContext serverSslContext;
    private SSLParameters serverSslParameters;

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @Path("more-secure")
    public static class TestResource {
        @GET
        public String get() {
            return "GET";
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
            serverSslContext = SslUtils.createServerSslContext(true, true);
        }

        return Optional.of(serverSslContext);
    }

    @Override
    protected Optional<SSLParameters> getSslParameters() {
        if (serverSslParameters == null) {
            serverSslParameters = new SSLParameters();
            serverSslParameters.setNeedClientAuth(true);
        }

        return Optional.of(serverSslParameters);
    }

    @Test
    public void testGet() {
        SSLContext clientSslContext = SslUtils.createClientSslContext(true, true);

        Client client = ClientBuilder.newBuilder()
                .sslContext(clientSslContext)
                .build();

        WebTarget target = client.target(getBaseUri()).path("more-secure");

        String s = target.request().get(String.class);
        Assertions.assertEquals("GET", s);
    }

    @Test
    public void testGetFailsWhenClientDoesNotTrustsServer() {
        Assertions.assertThrows(ProcessingException.class, () -> {
            SSLContext clientSslContext = SslUtils.createClientSslContext(true, false);

            Client client = ClientBuilder.newBuilder()
                    .sslContext(clientSslContext)
                    .build();

            WebTarget target = client.target(getBaseUri()).path("more-secure");

            target.request().get(String.class);
        });
    }

    @Test
    public void testGetFailsWhenClientCanNotIdentifyItselfToTheServer() {
        Assertions.assertThrows(ProcessingException.class, () -> {
            SSLContext clientSslContext = SslUtils.createClientSslContext(false, true);

            Client client = ClientBuilder.newBuilder()
                    .sslContext(clientSslContext)
                    .build();

            WebTarget target = client.target(getBaseUri()).path("more-secure");

            target.request().get(String.class);
        });
    }

    @Test
    public void testGetFailsWhenClientExecutesRequestWithoutHavingSslConfigured() {
        Assertions.assertThrows(ProcessingException.class, () -> {
            Client client = ClientBuilder.newClient();

            WebTarget target = client.target(getBaseUri()).path("more-secure");

            target.request().get(String.class);
        });
    }

}
