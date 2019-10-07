/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jmockit.server.internal;

import mockit.Mocked;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link RuntimeDelegate}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public class RuntimeDelegateImplTest {
    @Test
    public final void shouldCreateConfigurationBuilder() {
        // given
        final RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();

        // when
        final SeBootstrap.Configuration.Builder configurationBuilder = runtimeDelegate.createConfigurationBuilder();

        // then
        assertThat(configurationBuilder, is(notNullValue()));
    }

    @Test
    public final void shouldBuildDefaultConfiguration() throws NoSuchAlgorithmException {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.HOST));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PORT));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration.property(SeBootstrap.Configuration.PROTOCOL), is("HTTP"));
        assertThat(configuration.property(SeBootstrap.Configuration.HOST), is("localhost"));
        assertThat(configuration.property(SeBootstrap.Configuration.PORT), is(SeBootstrap.Configuration.DEFAULT_PORT));
        assertThat(configuration.property(SeBootstrap.Configuration.ROOT_PATH), is("/"));
        assertThat(configuration.property(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION),
                is(SeBootstrap.Configuration.SSLClientAuthentication.NONE));
        assertThat(configuration.property(SeBootstrap.Configuration.SSL_CONTEXT), is(theInstance(SSLContext.getDefault())));
        assertThat(configuration.protocol(), is("HTTP"));
        assertThat(configuration.host(), is("localhost"));
        assertThat(configuration.port(), is(SeBootstrap.Configuration.DEFAULT_PORT));
        assertThat(configuration.rootPath(), is("/"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.NONE));
        assertThat(configuration.sslContext(), is(theInstance(SSLContext.getDefault())));
    }

    @Test
    public final void shouldBuildConfigurationContainingCustomProperties() {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.property("property", "value").build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty("property"));
        assertThat(configuration.property("property"), is("value"));
    }

    @Test
    public final void shouldBuildCustomConfigurationUsingNamedStandardProperties(@Mocked final SSLContext mockSslContext)
            throws NoSuchAlgorithmException {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.property(SeBootstrap.Configuration.PROTOCOL, "HTTPS")
                .property(SeBootstrap.Configuration.HOST, "hostname").property(SeBootstrap.Configuration.PORT, 8080)
                .property(SeBootstrap.Configuration.ROOT_PATH, "path")
                .property(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION,
                        SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL)
                .property(SeBootstrap.Configuration.SSL_CONTEXT, mockSslContext).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.HOST));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PORT));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
    }

    @Test
    public final void shouldBuildCustomConfigurationUsingConvenienceMethods(@Mocked final SSLContext mockSslContext)
            throws NoSuchAlgorithmException {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();

        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.protocol("HTTPS").host("hostname").port(8080)
                .rootPath("path").sslClientAuthentication(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL)
                .sslContext(mockSslContext).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.HOST));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PORT));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
    }

    @Test
    public final void shouldBuildCustomConfigurationFromPropertiesProvider(@Mocked final SSLContext mockSslContext) {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        final Class<Server> mockServerClass = Server.class;
        final BiFunction<String, Class<Object>, Optional<Object>> propertiesProvider = (propertyName, propertyType) -> {
            if (SeBootstrap.Configuration.PROTOCOL.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("HTTPS");
            }
            if (SeBootstrap.Configuration.HOST.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("hostname");
            }
            if (SeBootstrap.Configuration.PORT.equals(propertyName) && Integer.class.equals(propertyType)) {
                return Optional.of(8080);
            }
            if (SeBootstrap.Configuration.ROOT_PATH.equals(propertyName) && String.class.equals(propertyType)) {
                return Optional.of("path");
            }
            if (SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION.equals(propertyName)
                    && SeBootstrap.Configuration.SSLClientAuthentication.class.equals(propertyType)) {
                return Optional.of(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL);
            }
            if (SeBootstrap.Configuration.SSL_CONTEXT.equals(propertyName) && SSLContext.class.equals(propertyType)) {
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
        final SeBootstrap.Configuration configuration = configurationBuilder.from(propertiesProvider).build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.HOST));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PORT));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration, hasProperty(ServerProperties.HTTP_SERVER_CLASS));
        assertThat(configuration, hasProperty(ServerProperties.AUTO_START));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8080));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
        assertThat(configuration.property(ServerProperties.HTTP_SERVER_CLASS), is(theInstance(mockServerClass)));
        assertThat(configuration.property(ServerProperties.AUTO_START), is(FALSE));
    }

    @Test
    public final void shouldBootstrapApplication(@Mocked final Container mockContainer,
                                                 @Mocked final Application mockApplication,
                                                 @Mocked final SSLContext mockSslContext) throws InterruptedException,
            ExecutionException, TimeoutException {
        // given
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
        final SeBootstrap.Configuration mockConfiguration = name -> {
            switch (name) {
                case SeBootstrap.Configuration.PROTOCOL:
                    return "HTTPS";
                case SeBootstrap.Configuration.HOST:
                    return "hostname";
                case SeBootstrap.Configuration.PORT:
                    return SeBootstrap.Configuration.DEFAULT_PORT;
                case SeBootstrap.Configuration.ROOT_PATH:
                    return "path";
                case SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION:
                    return SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL;
                case SeBootstrap.Configuration.SSL_CONTEXT:
                    return mockSslContext;
                case ServerProperties.HTTP_SERVER_CLASS:
                    return Server.class;
                default:
                    return null;
            }
        };
        ServiceFinder.setIteratorProvider(new ServiceFinder.ServiceIteratorProvider() {
            @Override
            public final <T> Iterator<T> createIterator(final Class<T> service, final String serviceName,
                                                        final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return Collections.singleton(service.cast(new ServerProvider() {
                    @Override
                    public final <U extends Server> U createServer(final Class<U> type, final Application application,
                                                                   final SeBootstrap.Configuration configuration) {
                        return application == mockApplication && configuration == mockConfiguration
                                ? type.cast(mockServer)
                                : null;
                    }
                })).iterator();
            }

            @Override
            public final <T> Iterator<Class<T>> createClassIterator(final Class<T> service,
                                                                    final String serviceName,
                                                                    final ClassLoader loader,
                                                                    final boolean ignoreOnClassNotFound) {
                return null;
            }
        });

        // when
        final CompletionStage<SeBootstrap.Instance> bootstrapStage = runtimeDelegate.bootstrap(mockApplication,
                mockConfiguration);
        final SeBootstrap.Instance instance = bootstrapStage.toCompletableFuture().get(15, SECONDS);
        final SeBootstrap.Configuration configuration = instance.configuration();
        final Server server = instance.unwrap(Server.class);
        final Container container = server.container();
        final CompletionStage<SeBootstrap.Instance.StopResult> stopStage = instance.stop();
        final Object stopResult = stopStage.toCompletableFuture().get(15, SECONDS);

        // then
        assertThat(instance, is(notNullValue()));
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.HOST));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.PORT));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertThat(configuration, hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration, hasProperty(ServerProperties.HTTP_SERVER_CLASS));
        assertThat(configuration.protocol(), is("HTTPS"));
        assertThat(configuration.host(), is("hostname"));
        assertThat(configuration.port(), is(8888));
        assertThat(configuration.rootPath(), is("path"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.OPTIONAL));
        assertThat(configuration.sslContext(), is(theInstance(mockSslContext)));
        assertThat(configuration.property(ServerProperties.HTTP_SERVER_CLASS), is(theInstance(mockServer.getClass())));
        assertThat(server, is(theInstance(mockServer)));
        assertThat(container, is(theInstance(mockContainer)));
        assertThat(stopResult, is(notNullValue()));
    }

    @After
    public final void resetServiceFinder() {
        ServiceFinder.setIteratorProvider(null);
    }

    /**
     * Creates a matcher that matches any examined object whose <code>hasProperty</code> method
     * returns {@code true} for the provided property name.
     * For example:
     * <pre>assertThat(configuration, hasProperty("HOST"))</pre>
     *
     * @param propertyName
     *     the property name to check
     */
    private static final Matcher<SeBootstrap.Configuration> hasProperty(final String propertyName) {
        return new FeatureMatcher<SeBootstrap.Configuration, Boolean>(is(TRUE), "hasProperty", "hasProperty") {
            @Override
            protected final Boolean featureValueOf(final SeBootstrap.Configuration actual) {
                return actual.hasProperty(propertyName);
            }
        };
    }
}
