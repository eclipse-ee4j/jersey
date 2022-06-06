/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.internal.config.ExternalPropertiesConfigurationFactory;
import org.glassfish.jersey.internal.config.SystemPropertiesConfigurationModel;
import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.WebServer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Jersey implementation of {@link SeBootstrap.Configuration} implementing arbitrary methods for acquiring
 * the configuration settings.
 * @since 3.1.0
 */
public final class JerseySeBootstrapConfiguration implements SeBootstrap.Configuration {
    private static final Logger LOGGER = Logger.getLogger(JerseySeBootstrapConfiguration.class.getName());
    protected static final Random RANDOM = new Random();
    private final SeBootstrap.Configuration configuration;

    private JerseySeBootstrapConfiguration(SeBootstrap.Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Object property(String name) {
        return configuration.property(name);
    }

    /**
     * Compose {@link URI} based on properties defined in this configuration.
     * @param resolveDefaultPort if {@code true} the port is not set, it is resolved as
     *                           {@link Container#DEFAULT_HTTP_PORT} or {@link Container#DEFAULT_HTTPS_PORT}
     *                           based on the protocol scheme.
     * @return Composed {@link URI} based on properties defined in this configuration.
     */
    public URI uri(boolean resolveDefaultPort) {
        final String protocol = configuration.protocol();
        final String host = configuration.host();
        final int port = resolveDefaultPort ? resolvePort() : configuration.port();
        final String rootPath = configuration.rootPath();
        final URI uri = UriBuilder.newInstance().scheme(protocol.toLowerCase()).host(host).port(port).path(rootPath)
                .build();
        return uri;
    }

    private int resolvePort() {
        final int configPort = configuration.port();
        final int basePort = allowPrivilegedPorts() ? 0 : 8000;
        final int port;
        switch (configPort) {
            case SeBootstrap.Configuration.DEFAULT_PORT:
                port = basePort + (isHttps() ? Container.DEFAULT_HTTPS_PORT : Container.DEFAULT_HTTP_PORT);
                break;
            case SeBootstrap.Configuration.FREE_PORT:
               port = _resolvePort(basePort == 0);
               break;
            default:
                port = configPort;
                break;
        }
        return port;
    }

    private int _resolvePort(boolean allowPrivilegedPort) {
        final int basePort = allowPrivilegedPort ? 0 : 1024;
        // Get the initial range parameters
        final int lower = basePort;
        final int range = 0xFFFF;

        // Select a start point in the range
        final int initialOffset = RANDOM.nextInt(range - lower);

        // Loop the offset through all ports in the range and attempt
        // to bind to each
        int offset = initialOffset;
        ServerSocket socket;
        do {
            final int port = lower + offset;
            try {
                socket = new ServerSocket(port);
                socket.close();
                return port;
            } catch (IOException caught) {
                // Swallow exceptions until the end
            }
            offset = (offset + 1) % range;
        } while (offset != initialOffset);

        // If a port can't be bound, throw the exception
        throw new IllegalArgumentException(LocalizationMessages.COULD_NOT_BIND_TO_ANY_PORT());
    }

    /**
     * Return {@link SSLContext} in the configuration if the protocol scheme is {@code HTTPS}.
     * @return the SSLContext in the configuration.
     */
    @Override
    public SSLContext sslContext() {
        final SSLContext sslContext = configuration.sslContext();
        return isHttps() ? sslContext : null;
    }

    /**
     * If the protocol schema is {@code HTTPS}, return {@code true}.
     * @return {@code true} when the protocol schema is {@code HTTPS}.
     */
    public boolean isHttps() {
        return "HTTPS".equalsIgnoreCase(configuration.protocol());
    }

    /**
     * Defines if the {@link WebServer} should automatically start.
     * @return false if {@link ServerProperties#WEBSERVER_AUTO_START} is {@code false}, {@code true} otherwise.
     */
    public boolean autoStart() {
        final boolean autoStart = Optional.ofNullable(
                (Boolean) configuration.property(ServerProperties.WEBSERVER_AUTO_START))
                .orElse(TRUE);
        return autoStart;
    }

    /**
     * Defines if the {@link WebServer} should start on a privileged port when port is not set.
     * @return true if {@link ServerProperties#WEBSERVER_AUTO_START} is {@code true}, {@code false} otherwise.
     */
    public boolean allowPrivilegedPorts() {
        return Optional.ofNullable(
                (Boolean) configuration.property(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS))
                .orElse(FALSE);
    }

    /**
     * Factory method creating {@code JerseySeBootstrapConfiguration} wrapper around {@link SeBootstrap.Configuration}.
     * @param configuration wrapped configuration
     * @return {@code JerseySeBootstrapConfiguration} wrapper around {@link SeBootstrap.Configuration}.
     */
    public static JerseySeBootstrapConfiguration from(SeBootstrap.Configuration configuration) {
        return JerseySeBootstrapConfiguration.class.isInstance(configuration)
                ? (JerseySeBootstrapConfiguration) configuration
                : new JerseySeBootstrapConfiguration(configuration);
    }

    /**
     * Return a Jersey instance of {@link SeBootstrap.Configuration.Builder} with prefilled values.
     * @return a Jersey instance of {@link SeBootstrap.Configuration.Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements SeBootstrap.Configuration.Builder {
        private static final Map<String, Class<?>> PROPERTY_TYPES = new HashMap<>();

        static {
            PROPERTY_TYPES.put(SeBootstrap.Configuration.PROTOCOL, String.class);
            PROPERTY_TYPES.put(SeBootstrap.Configuration.HOST, String.class);
            PROPERTY_TYPES.put(SeBootstrap.Configuration.PORT, Integer.class);
            PROPERTY_TYPES.put(SeBootstrap.Configuration.ROOT_PATH, String.class);
            PROPERTY_TYPES.put(SeBootstrap.Configuration.SSL_CONTEXT, SSLContext.class);
            PROPERTY_TYPES.put(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION, SSLClientAuthentication.class);
            PROPERTY_TYPES.put(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS, Boolean.class);
            PROPERTY_TYPES.put(ServerProperties.WEBSERVER_AUTO_START, Boolean.class);
            PROPERTY_TYPES.put(ServerProperties.WEBSERVER_CLASS, Class.class);
        }

        private final Map<String, Object> properties = new HashMap<>();

        private Builder() {
            this.properties.put(SeBootstrap.Configuration.PROTOCOL, "HTTP"); // upper case mandated by javadoc
            this.properties.put(SeBootstrap.Configuration.HOST, "localhost");
            this.properties.put(SeBootstrap.Configuration.PORT, -1); // Auto-select port 8080 for HTTP or 8443 for HTTPS
            this.properties.put(SeBootstrap.Configuration.ROOT_PATH, "/");
            this.properties.put(ServerProperties.WEBSERVER_CLASS, WebServer.class); // Auto-select first provider
            try {
                this.properties.put(SeBootstrap.Configuration.SSL_CONTEXT, SSLContext.getDefault());
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            this.properties.put(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION,
                    SeBootstrap.Configuration.SSLClientAuthentication.NONE);
            this.properties.put(ServerProperties.WEBSERVER_AUTO_START, TRUE);
            this.properties.put(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS, FALSE);

            SystemPropertiesConfigurationModel propertiesConfigurationModel = new SystemPropertiesConfigurationModel(
                    Collections.singletonList(Properties.class.getName())
            );
            from((name, aClass) -> String.class.equals(aClass) || Integer.class.equals(aClass) || Boolean.class.equals(aClass)
                    ? propertiesConfigurationModel.getOptionalProperty(name, aClass)
                    : Optional.empty()
            );
        }

        @Override
        public JerseySeBootstrapConfiguration build() {
            return JerseySeBootstrapConfiguration.from(this.properties::get);
        }

        @Override
        public Builder property(String name, Object value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * Set the the respective {@link WebServer} class to be used by the
         * {@link org.glassfish.jersey.server.spi.WebServerProvider}.
         * @param webServerClass the class implementing {@link WebServer}.
         * @return the updated builder.
         */
        public Builder webServerClass(Class<? extends WebServer> webServerClass) {
            return property(ServerProperties.WEBSERVER_CLASS, webServerClass);
        }

        /**
         * Define if the {@link WebServer} should auto-start at bootstrap.
         * @param autostart the auto-start flag.
         * @return the updated builder.
         */
        public Builder autoStart(Boolean autostart) {
            return property(ServerProperties.WEBSERVER_AUTO_START, autostart);
        }

        @Override
        public <T> JerseySeBootstrapConfiguration.Builder from(BiFunction<String, Class<T>, Optional<T>> configProvider) {
            PROPERTY_TYPES.forEach(
                    (propertyName, propertyType) -> configProvider.apply(propertyName, (Class<T>) propertyType)
                            .ifPresent(propertyValue -> this.properties.put(propertyName, propertyValue)));
            return this;
        }

        @Override
        public JerseySeBootstrapConfiguration.Builder from(Object externalConfig) {
            if (SeBootstrap.Configuration.class.isInstance(externalConfig)) {
                final SeBootstrap.Configuration other = (SeBootstrap.Configuration) externalConfig;
                from((name, clazz) -> {
                    final Object property = other.property(name);
                    if (property != null) {
                        if (clazz.equals(property.getClass())) {
                            return Optional.of(property);
                        } else {
                            LOGGER.warning(LocalizationMessages.IGNORE_SEBOOTSTRAP_CONFIGURATION_PROPERTY(name, clazz));
                        }
                    }
                    return Optional.empty();
                });
            }
            return this;
        }
    }

    /**
     * Name the properties to be internally read from System properties by {@link ExternalPropertiesConfigurationFactory}.
     * This is required just when SecurityManager is on, otherwise all system properties are read.
     */
    @PropertiesClass
    private static class Properties {
        /**
         * See {@link SeBootstrap.Configuration#PROTOCOL} property.
         */
        public static final String SE_BOOTSTRAP_CONFIGURATION_PROTOCOL = SeBootstrap.Configuration.PROTOCOL;

        /**
         * See {@link SeBootstrap.Configuration#HOST} property.
         */
        public static final String SE_BOOTSTRAP_CONFIGURATION_HOST = SeBootstrap.Configuration.HOST;

        /**
         * See {@link SeBootstrap.Configuration#PORT} property.
         */
        public static final String SE_BOOTSTRAP_CONFIGURATION_PORT = SeBootstrap.Configuration.PORT;

        /**
         * See {@link SeBootstrap.Configuration#ROOT_PATH} property.
         */
        public static final String SE_BOOTSTRAP_CONFIGURATION_ROOT_PATH = SeBootstrap.Configuration.ROOT_PATH;

        /**
         * See {@link ServerProperties#WEBSERVER_ALLOW_PRIVILEGED_PORTS} property.
         */
        public static final String WEBSERVER_ALLOW_PRIVILEGED_PORTS  = ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS;

        /**
         * See {@link ServerProperties#WEBSERVER_AUTO_START} property.
         */
        public static final String WEBSERVER_AUTO_START = ServerProperties.WEBSERVER_AUTO_START;
    }
}
