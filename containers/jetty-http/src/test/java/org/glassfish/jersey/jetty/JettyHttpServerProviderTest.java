/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.jetty;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.JAXRS;
import jakarta.ws.rs.JAXRS.Configuration.SSLClientAuthentication;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;
import org.junit.Test;

/**
 * Unit tests for {@link JettyHttpServerProvider}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.30
 */
public final class JettyHttpServerProviderTest {

    @Test(timeout = 15000)
    public final void shouldProvideServer() throws InterruptedException, ExecutionException {
        // given
        final ServerProvider serverProvider = new JettyHttpServerProvider();
        final Resource resource = new Resource();
        final Application application = new Application() {
            @Override
            public final Set<Object> getSingletons() {
                return Collections.singleton(resource);
            }
        };
        final JAXRS.Configuration configuration = name -> {
            switch (name) {
            case JAXRS.Configuration.PROTOCOL:
                return "HTTP";
            case JAXRS.Configuration.HOST:
                return "localhost";
            case JAXRS.Configuration.PORT:
                return getPort();
            case JAXRS.Configuration.ROOT_PATH:
                return "/";
            case JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION:
                return SSLClientAuthentication.NONE;
            case JAXRS.Configuration.SSL_CONTEXT:
                try {
                    return SSLContext.getDefault();
                } catch (final NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            case ServerProperties.AUTO_START:
                return FALSE;
            default:
                return null;
            }
        };

        // when
        final Server server = serverProvider.createServer(Server.class, application, configuration);
        final Object nativeHandle = server.unwrap(Object.class);
        final CompletionStage<?> start = server.start();
        final Object startResult = start.toCompletableFuture().get();
        final Container container = server.container();
        final int port = server.port();
        final String entity = ClientBuilder.newClient()
                .target(UriBuilder.newInstance().scheme("http").host("localhost").port(port).build()).request()
                .get(String.class);
        final CompletionStage<?> stop = server.stop();
        final Object stopResult = stop.toCompletableFuture().get();

        // then
        assertThat(server, is(instanceOf(JettyHttpServer.class)));
        assertThat(nativeHandle, is(instanceOf(org.eclipse.jetty.server.Server.class)));
        assertThat(startResult, is(nullValue()));
        assertThat(container, is(instanceOf(JettyHttpContainer.class)));
        assertThat(port, is(greaterThan(0)));
        assertThat(entity, is(resource.toString()));
        assertThat(stopResult, is(nullValue()));
    }

    @Path("/")
    protected static final class Resource {
        @GET
        @Override
        public final String toString() {
            return super.toString();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JettyHttpServerProviderTest.class.getName());

    private static final int DEFAULT_PORT = 0;

    private static final int getPort() {
        final String value = AccessController
                .doPrivileged(PropertiesHelper.getSystemProperty("jersey.config.test.container.port"));
        if (value != null) {
            try {
                final int i = Integer.parseInt(value);
                if (i < 0) {
                    throw new NumberFormatException("Value is negative.");
                }
                return i;
            } catch (final NumberFormatException e) {
                LOGGER.log(Level.CONFIG,
                        "Value of 'jersey.config.test.container.port'"
                                + " property is not a valid non-negative integer [" + value + "]."
                                + " Reverting to default [" + DEFAULT_PORT + "].",
                        e);
            }
        }

        return DEFAULT_PORT;
    }

    @Test(timeout = 15000)
    public final void shouldScanFreePort() throws InterruptedException, ExecutionException {
        // given
        final ServerProvider serverProvider = new JettyHttpServerProvider();
        final Application application = new Application();
        final JAXRS.Configuration configuration = name -> {
            switch (name) {
            case JAXRS.Configuration.PROTOCOL:
                return "HTTP";
            case JAXRS.Configuration.HOST:
                return "localhost";
            case JAXRS.Configuration.PORT:
                return JAXRS.Configuration.FREE_PORT;
            case JAXRS.Configuration.ROOT_PATH:
                return "/";
            case JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION:
                return SSLClientAuthentication.NONE;
            case JAXRS.Configuration.SSL_CONTEXT:
                try {
                    return SSLContext.getDefault();
                } catch (final NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            case ServerProperties.AUTO_START:
                return TRUE;
            default:
                return null;
            }
        };

        // when
        final Server server = serverProvider.createServer(Server.class, application, configuration);

        // then
        assertThat(server.port(), is(greaterThan(0)));
    }

}
