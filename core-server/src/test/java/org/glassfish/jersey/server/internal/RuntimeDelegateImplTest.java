/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal;

import static java.lang.Boolean.FALSE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;
import org.junit.Test;

/**
 * Unit tests for {@link RuntimeDelegate}.
 *
 * @author Martin Matula
 * @author Markus KARG (markus@headcrashing.eu)
 */
public class RuntimeDelegateImplTest {

    @Test
    public void testCreateEndpoint() {
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();
        try {
            delegate.createEndpoint((Application) null, com.sun.net.httpserver.HttpHandler.class);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException iae) {
            // ok - should be thrown
        } catch (Exception e) {
            fail("IllegalArgumentException should be thrown");
        }
    }

    /**
     * Checks that the right RuntimeDelegateImpl is loaded by JAX-RS.
     */
    @Test
    public void testRuntimeDelegateInstance() {
        assertSame(RuntimeDelegateImpl.class, RuntimeDelegate.getInstance().getClass());
    }

    @Test
    public final void shouldCreateConfigurationBuilder() {
        // given
        final RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();

        // when
        final JAXRS.Configuration.Builder configurationBuilder = runtimeDelegate.createConfigurationBuilder();

        // then
        assertThat(configurationBuilder, is(notNullValue()));
    }

    @Test
    public final void shouldBuildDefaultConfiguration() throws NoSuchAlgorithmException {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final JAXRS.Configuration configuration = configurationBuilder.build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.property(JAXRS.Configuration.PROTOCOL), is("HTTP"));
        assertThat(configuration.property(JAXRS.Configuration.HOST), is("localhost"));
        assertThat(configuration.property(JAXRS.Configuration.PORT), is(JAXRS.Configuration.DEFAULT_PORT));
        assertThat(configuration.property(JAXRS.Configuration.ROOT_PATH), is("/"));
        assertThat(configuration.property(JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION),
                is(JAXRS.Configuration.SSLClientAuthentication.NONE));
        assertThat(configuration.property(JAXRS.Configuration.SSL_CONTEXT), is(theInstance(SSLContext.getDefault())));
        assertThat(configuration.protocol(), is("HTTP"));
        assertThat(configuration.host(), is("localhost"));
        assertThat(configuration.port(), is(JAXRS.Configuration.DEFAULT_PORT));
        assertThat(configuration.rootPath(), is("/"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.NONE));
        assertThat(configuration.sslContext(), is(theInstance(SSLContext.getDefault())));
    }

    @Test
    public final void shouldBuildConfigurationContainingCustomProperties() {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final JAXRS.Configuration configuration = configurationBuilder.property("property", "value").build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.property("property"), is("value"));
    }

    @Test
    public final void shouldBuildCustomConfigurationUsingNamedStandardProperties() throws NoSuchAlgorithmException {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        final SSLContext mockSslContext = new SSLContext(null, null, null) {
        };

        // when
        final JAXRS.Configuration configuration = configurationBuilder.property(JAXRS.Configuration.PROTOCOL, "HTTPS")
                .property(JAXRS.Configuration.HOST, "hostname").property(JAXRS.Configuration.PORT, 8080)
                .property(JAXRS.Configuration.ROOT_PATH, "path")
                .property(JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION,
                        JAXRS.Configuration.SSLClientAuthentication.OPTIONAL)
                .property(JAXRS.Configuration.SSL_CONTEXT, mockSslContext).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
    }

    @Test
    public final void shouldBuildCustomConfigurationUsingConvenienceMethods() throws NoSuchAlgorithmException {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        final SSLContext mockSslContext = new SSLContext(null, null, null) {
        };

        // when
        final JAXRS.Configuration configuration = configurationBuilder.protocol("HTTPS").host("hostname").port(8080)
                .rootPath("path").sslClientAuthentication(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL)
                .sslContext(mockSslContext).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
    }

    @Test
    public final void shouldBuildCustomConfigurationFromPropertiesProvider() {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        final SSLContext mockSslContext = new SSLContext(null, null, null) {
        };
        final Class<Server> mockServerClass = Server.class;
        final BiFunction<String, Class<Object>, Optional<Object>> propertiesProvider = (propertyName, propertyType) -> {
            if (JAXRS.Configuration.PROTOCOL.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("HTTPS");
            }
            if (JAXRS.Configuration.HOST.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("hostname");
            }
            if (JAXRS.Configuration.PORT.equals(propertyName) && Integer.class.equals(propertyType)) {
                return Optional.of(8080);
            }
            if (JAXRS.Configuration.ROOT_PATH.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("path");
            }
            if (JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION.equals(propertyName)
                    && JAXRS.Configuration.SSLClientAuthentication.class.equals(propertyType)) {
                return Optional.of(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL);
            }
            if (JAXRS.Configuration.SSL_CONTEXT.equals(propertyName) && SSLContext.class.equals(propertyType)) {
                return Optional.of(mockSslContext);
            }
            if (ServerProperties.HTTP_SERVER_CLASS.equals(propertyName) && Class.class.equals(propertyType)) {
                return Optional.of(mockServerClass);
            }
            if (ServerProperties.AUTO_START.equals(propertyName) && Boolean.class.equals(propertyType)) {
                return Optional.of(FALSE);
            }
            return Optional.empty();
        };

        // when
        final JAXRS.Configuration configuration = configurationBuilder.from(propertiesProvider).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
        assertThat(configuration.property(ServerProperties.HTTP_SERVER_CLASS), is(theInstance(mockServerClass)));
        assertThat(configuration.property(ServerProperties.AUTO_START), is(FALSE));
    }

    @Test
    public final void shouldBuildCustomConfigurationFromMicroprofileConfig() {
        // given
        final JAXRS.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        final SSLContext mockSslContext = new SSLContext(null, null, null) {
        };
        final Class<Server> mockServerClass = Server.class;
        final Config config = new Config() {
            @Override
            public final <T> T getValue(final String propertyName, final Class<T> propertyType) {
                return null;
            }

            @Override
            public final <T> Optional<T> getOptionalValue(final String propertyName, final Class<T> propertyType) {
                if (JAXRS.Configuration.PROTOCOL.equals(propertyName) && String.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast("HTTPS"));
                }
                if (JAXRS.Configuration.HOST.equals(propertyName) && String.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast("hostname"));
                }
                if (JAXRS.Configuration.PORT.equals(propertyName) && Integer.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast(8080));
                }
                if (JAXRS.Configuration.ROOT_PATH.equals(propertyName) && String.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast("path"));
                }
                if (JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION.equals(propertyName)
                        && JAXRS.Configuration.SSLClientAuthentication.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
                }
                if (JAXRS.Configuration.SSL_CONTEXT.equals(propertyName) && SSLContext.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast(mockSslContext));
                }
                if (ServerProperties.HTTP_SERVER_CLASS.equals(propertyName) && Class.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast(mockServerClass));
                }
                if (ServerProperties.AUTO_START.equals(propertyName) && Boolean.class.equals(propertyType)) {
                    return Optional.of(propertyType.cast(FALSE));
                }
                return Optional.empty();
            }

            @Override
            public final Iterable<String> getPropertyNames() {
                return null;
            }

            @Override
            public final Iterable<ConfigSource> getConfigSources() {
                return null;
            }
        };

        // when
        final JAXRS.Configuration configuration = configurationBuilder.from(config).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
        assertThat(configuration.property(ServerProperties.HTTP_SERVER_CLASS), is(theInstance(mockServerClass)));
        assertThat(configuration.property(ServerProperties.AUTO_START), is(FALSE));
    }

    @Test
    public final void shouldBootstrapApplication() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        final Container mockContainer = new Container() {
            @Override
            public final ResourceConfig getConfiguration() {
                return null;
            }

            @Override
            public final ApplicationHandler getApplicationHandler() {
                return null;
            }

            @Override
            public final void reload() {
            }

            @Override
            public final void reload(final ResourceConfig configuration) {
            }
        };
        final Server mockServer = new Server() {
            @Override
            public final Container container() {
                return mockContainer;
            }

            @Override
            public final int port() {
                return 8888;
            }

            @Override
            public final CompletionStage<?> start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public final CompletionStage<?> stop() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public final <T> T unwrap(final Class<T> nativeClass) {
                return null;
            }
        };
        final RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();
        final Application mockApplication = new Application();
        final SSLContext mockSslContext = new SSLContext(null, null, null) {
        };
        final JAXRS.Configuration mockConfiguration = name -> {
            switch (name) {
            case JAXRS.Configuration.PROTOCOL:
                return "HTTPS";
            case JAXRS.Configuration.HOST:
                return "hostname";
            case JAXRS.Configuration.PORT:
                return JAXRS.Configuration.DEFAULT_PORT;
            case JAXRS.Configuration.ROOT_PATH:
                return "path";
            case JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION:
                return JAXRS.Configuration.SSLClientAuthentication.OPTIONAL;
            case JAXRS.Configuration.SSL_CONTEXT:
                return mockSslContext;
            case ServerProperties.HTTP_SERVER_CLASS:
                return Server.class;
            default:
                return null;
            }
        };
        ServiceFinder.setIteratorProvider(new ServiceIteratorProvider() {
            @Override
            public final <T> Iterator<T> createIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return Collections.singleton(service.cast(new ServerProvider() {
                    @Override
                    public final <U extends Server> U createServer(final Class<U> type, final Application application,
                            final Configuration configuration) {
                        return application == mockApplication && configuration == mockConfiguration
                                ? type.cast(mockServer)
                                : null;
                    }
                })).iterator();
            }

            @Override
            public final <T> Iterator<Class<T>> createClassIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return null;
            }
        });

        // when
        final CompletionStage<JAXRS.Instance> bootstrapStage = runtimeDelegate.bootstrap(mockApplication,
                mockConfiguration);
        final JAXRS.Instance instance = bootstrapStage.toCompletableFuture().get(15, SECONDS);
        final Configuration configuration = instance.configuration();
        final Server server = instance.unwrap(Server.class);
        final Container container = server.container();
        final CompletionStage<JAXRS.Instance.StopResult> stopStage = instance.stop();
        final Object stopResult = stopStage.toCompletableFuture().get(15, SECONDS);

        // then
        assertThat(instance, is(notNullValue()));
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8888));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(JAXRS.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
        assertThat(configuration.property(ServerProperties.HTTP_SERVER_CLASS), is(theInstance(mockServer.getClass())));
        assertThat(server, is(theInstance(mockServer)));
        assertThat(container, is(theInstance(mockContainer)));
        assertThat(stopResult, is(notNullValue()));
    }

}
