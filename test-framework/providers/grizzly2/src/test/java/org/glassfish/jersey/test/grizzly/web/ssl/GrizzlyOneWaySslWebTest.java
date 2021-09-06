/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

/**
 * Basic GrizzlyWebTestContainerFactory unit tests.
 *
 * The test contains a server configured with SSL at https://localhost:9998/
 * With one way authentication the client will validate the identity of the server with
 * the trusted certificates within the trust store of the client. If the server is trusted
 * by the client the ssl handshake process will proceed or else the request will fail
 *
 * @author Hakan Altindag
 */
public class GrizzlyOneWaySslWebTest extends JerseyTest {

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
            serverSslContext = SslUtils.createServerSslContext(true, false);
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

    @Test
    public void testGet() {
        SSLContext clientSslContext = SslUtils.createClientSslContext(false, true);

        Client client = ClientBuilder.newBuilder()
                .sslContext(clientSslContext)
                .build();

        WebTarget target = client.target(getBaseUri()).path("secure");

        String s = target.request().get(String.class);
        Assert.assertEquals("GET", s);
    }

    @Test
    public void testGetFailsWhenClientDoesNotTrustsServer() {
        SSLContext clientSslContext = SslUtils.createClientSslContext(false, false);

        Client client = ClientBuilder.newBuilder()
                .sslContext(clientSslContext)
                .build();

        WebTarget target = client.target(getBaseUri()).path("secure");

        exception.expect(ProcessingException.class);

        target.request().get(String.class);
    }

    @Test
    public void testGetFailsWhenClientExecutesRequestWithoutHavingSslConfigured() {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(getBaseUri()).path("secure");

        exception.expect(ProcessingException.class);

        target.request().get(String.class);
    }

}
