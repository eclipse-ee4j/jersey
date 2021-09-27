/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

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
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.SeBootstrap.Configuration.SSLClientAuthentication;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.WebServer;
import org.glassfish.jersey.server.spi.WebServerProvider;
import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

/**
 * Unit tests for {@link NettyHttpServerProvider}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1.0
 */
public final class NettyHttpServerProviderTest {

    @Test(timeout = 15000)
    public void shouldProvideServer2() throws InterruptedException, ExecutionException {
        // given
        final Resource resource = new Resource();
        shouldProvideServer(ShouldProvideServerApplication.class, resource);
    }

    @Test(timeout = 15000)
    public void shouldProvideServerWithClass() throws InterruptedException, ExecutionException {
        // given
        final Resource resource = new Resource();
        final Application application = new ShouldProvideServerApplication();
        shouldProvideServer(application.getClass(), resource);
    }

    private void shouldProvideServer(final Object application, final Resource resource)
            throws InterruptedException, ExecutionException {
        // given
        final WebServerProvider webServerProvider = new NettyHttpServerProvider();
        final SeBootstrap.Configuration configuration = configuration(getPort(), FALSE);

        // when
        final JerseySeBootstrapConfiguration jerseySeConfig = JerseySeBootstrapConfiguration.from(configuration);
        final WebServer webServer = Application.class.isInstance(application)
                ? webServerProvider.createServer(WebServer.class, (Application) application, jerseySeConfig)
                : webServerProvider.createServer(WebServer.class, (Class<Application>) application, jerseySeConfig);
        final Object nativeHandle = webServer.unwrap(Object.class);
        final CompletionStage<?> start = webServer.start();
        final Object startResult = start.toCompletableFuture().get();
        final Container container = webServer.container();
        final int port = webServer.port();
        final String entity = ClientBuilder.newClient()
                .target(UriBuilder.newInstance().scheme("http").host("localhost").port(port).build()).request()
                .get(String.class);
        final CompletionStage<?> stop = webServer.stop();
        final Object stopResult = stop.toCompletableFuture().get();

        // then
        assertThat(webServer, is(instanceOf(NettyHttpServer.class)));
        assertThat(nativeHandle, is(instanceOf(ServerBootstrap.class)));
        assertThat(startResult, is(instanceOf(Channel.class)));
        assertThat(container, is(instanceOf(NettyHttpContainer.class)));
        assertThat(port, is(greaterThan(0)));
        assertThat(entity, is(resource.toString()));
        assertThat(stopResult, is(nullValue()));
    }


    @Path("/")
    protected static final class Resource {
        @GET
        @Override
        public String toString() {
            return Resource.class.getName();
        }
    }

    protected static class ShouldProvideServerApplication extends Application {
        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new Resource());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(NettyHttpServerProviderTest.class.getName());

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
        final WebServerProvider webServerProvider = new NettyHttpServerProvider();
        final Application application = new Application();
        final SeBootstrap.Configuration configuration = configuration(SeBootstrap.Configuration.FREE_PORT, TRUE);

        // when
        final JerseySeBootstrapConfiguration jerseySeConfig = JerseySeBootstrapConfiguration.from(configuration);
        final WebServer webServer = webServerProvider.createServer(WebServer.class, application, jerseySeConfig);

        // then
        assertThat(webServer.port(), is(greaterThan(0)));
    }

    private SeBootstrap.Configuration configuration(int port, boolean autoStart) {
        return (SeBootstrap.Configuration) name -> {
            switch (name) {
                case SeBootstrap.Configuration.PROTOCOL:
                    return "HTTP";
                case SeBootstrap.Configuration.HOST:
                    return "localhost";
                case SeBootstrap.Configuration.PORT:
                    return port;
                case SeBootstrap.Configuration.ROOT_PATH:
                    return "/";
                case SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION:
                    return SSLClientAuthentication.NONE;
                case SeBootstrap.Configuration.SSL_CONTEXT:
                    try {
                        return SSLContext.getDefault();
                    } catch (final NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                case ServerProperties.WEBSERVER_AUTO_START:
                    return autoStart;
                default:
                    return null;
            }
        };
    }

}
