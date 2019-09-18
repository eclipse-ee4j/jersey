/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

import static java.lang.Boolean.TRUE;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import javax.net.ssl.SSLContext;
import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.JAXRS.Configuration.Builder;
import javax.ws.rs.JAXRS.Configuration.SSLClientAuthentication;
import javax.ws.rs.core.Application;
import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Instance;

import org.glassfish.jersey.internal.AbstractRuntimeDelegate;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ServerFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.Server;

/**
 * Server-side implementation of JAX-RS {@link javax.ws.rs.ext.RuntimeDelegate}.
 * This overrides the default implementation of
 * {@link javax.ws.rs.ext.RuntimeDelegate} from jersey-common which does not
 * implement
 * {@link #createEndpoint(javax.ws.rs.core.Application, java.lang.Class)}
 * method.
 *
 * @author Jakub Podlesak
 * @author Marek Potociar
 * @author Martin Matula
 */
public class RuntimeDelegateImpl extends AbstractRuntimeDelegate {

    public RuntimeDelegateImpl() {
        super(new MessagingBinders.HeaderDelegateProviders().getHeaderDelegateProviders());
    }

    @Override
    public <T> T createEndpoint(final Application application, final Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }
        return ContainerFactory.createContainer(endpointType, application);
    }

    private static final Map<String, Class<?>> PROPERTY_TYPES = new HashMap<>();

    static {
        PROPERTY_TYPES.put(JAXRS.Configuration.PROTOCOL, String.class);
        PROPERTY_TYPES.put(JAXRS.Configuration.HOST, String.class);
        PROPERTY_TYPES.put(JAXRS.Configuration.PORT, Integer.class);
        PROPERTY_TYPES.put(JAXRS.Configuration.ROOT_PATH, String.class);
        PROPERTY_TYPES.put(JAXRS.Configuration.SSL_CONTEXT, SSLContext.class);
        PROPERTY_TYPES.put(JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION, SSLClientAuthentication.class);
        PROPERTY_TYPES.put(ServerProperties.HTTP_SERVER_CLASS, Class.class);
        PROPERTY_TYPES.put(ServerProperties.AUTO_START, Boolean.class);
    }

    @Override
    public JAXRS.Configuration.Builder createConfigurationBuilder() {
        return new JAXRS.Configuration.Builder() {
            private final Map<String, Object> properties = new HashMap<>();

            {
                this.properties.put(JAXRS.Configuration.PROTOCOL, "HTTP");
                this.properties.put(JAXRS.Configuration.HOST, "localhost");
                this.properties.put(JAXRS.Configuration.PORT, -1); // Auto-select port 80 for HTTP or 443 for HTTPS
                this.properties.put(JAXRS.Configuration.ROOT_PATH, "/");
                this.properties.put(ServerProperties.HTTP_SERVER_CLASS, Server.class); // Auto-select first provider
                try {
                    this.properties.put(JAXRS.Configuration.SSL_CONTEXT, SSLContext.getDefault());
                } catch (final NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                this.properties.put(JAXRS.Configuration.SSL_CLIENT_AUTHENTICATION,
                        JAXRS.Configuration.SSLClientAuthentication.NONE);
                this.properties.put(ServerProperties.AUTO_START, TRUE);
            }

            @Override
            public final JAXRS.Configuration.Builder property(final String name, final Object value) {
                this.properties.put(name, value);
                return this;
            }

            @SuppressWarnings("unchecked")
            @Override
            public final <T> Builder from(final BiFunction<String, Class<T>, Optional<T>> configProvider) {
                PROPERTY_TYPES.forEach(
                        (propertyName, propertyType) -> configProvider.apply(propertyName, (Class<T>) propertyType)
                                .ifPresent(propertyValue -> this.properties.put(propertyName, propertyValue)));
                return this;
            }

            @Override
            public final JAXRS.Configuration build() {
                return new JAXRS.Configuration() {
                    @Override
                    public final boolean hasProperty(final String name) {
                        return properties.containsKey(name);
                    }

                    @Override
                    public final Object property(final String name) {
                        return properties.get(name);
                    }
                };
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<JAXRS.Instance> bootstrap(final Application application,
            final JAXRS.Configuration configuration) {
        return CompletableFuture.supplyAsync(() -> {
            final Class<Server> httpServerClass = (Class<Server>) configuration
                    .property(ServerProperties.HTTP_SERVER_CLASS);

            return new JAXRS.Instance() {
                private final Server server = ServerFactory.createServer(httpServerClass, application, configuration);

                @Override
                public final Configuration configuration() {
                    return new Configuration() {
                        @Override
                        public final boolean hasProperty(final String name) {
                            switch (name) {
                            case JAXRS.Configuration.PORT:
                            case ServerProperties.HTTP_SERVER_CLASS:
                                return true;
                            default:
                                return configuration.hasProperty(name);
                            }
                        }

                        @Override
                        public final Object property(final String name) {
                            switch (name) {
                            case JAXRS.Configuration.PORT:
                                return server.port();
                            case ServerProperties.HTTP_SERVER_CLASS:
                                return server.getClass();
                            default:
                                return configuration.property(name);
                            }
                        }
                    };
                }

                @Override
                public final CompletionStage<StopResult> stop() {
                    return this.server.stop().thenApply(nativeResult -> new StopResult() {

                        @Override
                        public final <T> T unwrap(final Class<T> nativeClass) {
                            return nativeClass.cast(nativeResult);
                        }
                    });
                }

                @Override
                public final <T> T unwrap(final Class<T> nativeClass) {
                    return nativeClass.isInstance(this.server) ? nativeClass.cast(this.server)
                            : this.server.unwrap(nativeClass);
                }
            };
        });
    }

}
