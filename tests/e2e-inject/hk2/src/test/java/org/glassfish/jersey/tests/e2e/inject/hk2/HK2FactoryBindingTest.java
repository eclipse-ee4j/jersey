/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.hk2;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.InjectionManagerClientProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.function.Supplier;

public class HK2FactoryBindingTest {

    public static interface ConfigurationProperties {
        Map<String, Object> getProperties();
    }

    public static class ConfigurationPropertiesFactory implements org.glassfish.hk2.api.Factory<ConfigurationProperties> {

        private final Configuration configuration;

        @Inject
        public ConfigurationPropertiesFactory(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public ConfigurationProperties provide() {
            return new ConfigurationProperties() {
                @Override
                public Map<String, Object> getProperties() {
                    return configuration.getProperties();
                }
            };
        }

        @Override
        public void dispose(ConfigurationProperties configurationProperties) {

        }
    }

    @Priority(Priorities.USER)
    public static class ConfigurationPropertiesFilter implements ClientRequestFilter {
        @Inject
        ConfigurationProperties properties;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok(properties.getProperties().get(PROPERTY_NAME)).build());
        }
    }

    private static final String PROPERTY_NAME = "TEST_PROPERTY";
    private static final String PROPERTY_VALUE = "HELLO_PROPERTY";

    @Test
    public void testFactoryClassBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConfigurationPropertiesFactory.class).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    @Test
    public void testFactoryInstanceBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new ConfigurationPropertiesFactory(config)).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    static final class ConfigurationPropertiesImpl implements ConfigurationProperties {
        private final Configuration configuration;
        @Inject
        public ConfigurationPropertiesImpl(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Map<String, Object> getProperties() {
            return configuration.getProperties();
        }
    }

    static class ConfigurationPropertiesSupplier implements Supplier<ConfigurationPropertiesImpl> {
        private final Configuration configuration;

        @Inject
        ConfigurationPropertiesSupplier(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public ConfigurationPropertiesImpl get() {
            return new ConfigurationPropertiesImpl(configuration);
        }
    }

    @Test
    public void testSupplierJerseyInstanceBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(() -> new ConfigurationPropertiesImpl(config)).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    @Test
    public void testSupplierJerseyClassBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(ConfigurationPropertiesSupplier.class).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    public static interface ConfigurationPropertiesProvider {
        Map<String, Object> getProperties();

        ConfigurationProperties getConfigurationProperties();
    }

    public static class ConfigurationPropertiesProviderImpl implements ConfigurationPropertiesProvider {
        private final Configuration configuration;
        @Inject
        public ConfigurationPropertiesProviderImpl(Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Map<String, Object> getProperties() {
            return configuration.getProperties();
        }

        @Override
        public ConfigurationProperties getConfigurationProperties() {
            return new ConfigurationPropertiesImpl(configuration);
        }
    }

    public static class ConfigurationPropertiesProviderSupplier implements Supplier<ConfigurationProperties> {
        final ConfigurationPropertiesProvider impl;

        @Inject
        public ConfigurationPropertiesProviderSupplier(ConfigurationPropertiesProvider impl) {
            this.impl = impl;
        }

        @Override
        public ConfigurationProperties get() {
            return impl.getConfigurationProperties();
        }
    }

    public static class ConfigurationProperties2Filter implements ClientRequestFilter {
        @Inject
        ConfigurationProperties properties;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok(properties.getProperties().get(PROPERTY_NAME)).build());
        }
    }

    @Test
    public void testSupplierOfProviderClassBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConfigurationPropertiesProviderImpl.class).to(ConfigurationPropertiesProvider.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
                bindFactory(ConfigurationPropertiesProviderSupplier.class).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationProperties2Filter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    @Test
    public void testFactoryHk2ClassBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new org.glassfish.hk2.utilities.binding.AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(ConfigurationPropertiesFactory.class).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    @Test
    public void testFactoryHk2InstanceBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new org.glassfish.hk2.utilities.binding.AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new ConfigurationPropertiesFactory(config)).to(ConfigurationProperties.class).proxy(true)
                        .proxyForSameScope(false).in(RequestScoped.class);
            }
        }).register(ConfigurationPropertiesFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    static @interface ConfigurationPropertiesInject {

    }

    static class ConfigurationPropertiesInjectResolver implements InjectionResolver<ConfigurationPropertiesInject> {

        private final InjectionManager injectionManager;

        ConfigurationPropertiesInjectResolver(InjectionManager injectionManager) {
            this.injectionManager = injectionManager;
        }

        @Override
        public Object resolve(Injectee injectee) {
            if (injectee.getRequiredType() == ConfigurationProperties.class) {
                return new ConfigurationPropertiesImpl(injectionManager.getInstance(Configuration.class));
            }
            return null;
        }

        @Override
        public boolean isConstructorParameterIndicator() {
            return false;
        }

        @Override
        public boolean isMethodParameterIndicator() {
            return false;
        }

        @Override
        public Class<ConfigurationPropertiesInject> getAnnotation() {
            return ConfigurationPropertiesInject.class;
        }
    }

    public static class ConfigurationPropertiesInjectFilter implements ClientRequestFilter {
        @ConfigurationPropertiesInject
        ConfigurationProperties properties;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok(properties.getProperties().get(PROPERTY_NAME)).build());
        }
    }

    @Test
    public void testInjectionResolverBinding() {
        ClientConfig config = new ClientConfig();
        config.property(PROPERTY_NAME, PROPERTY_VALUE);
        String response = ClientBuilder.newClient(config).register(new Feature() {
            @Override
            public boolean configure(FeatureContext context) {
                final InjectionManager injectionManager = InjectionManagerClientProvider.getInjectionManager(context);
                context.register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(new ConfigurationPropertiesInjectResolver(injectionManager))
                                //.to(ConfigurationProperties.class)
                                .to(new GenericType<ConfigurationProperties>(){})
                                .in(Singleton.class);
                    }
                });
                return true;
            }
        }).register(ConfigurationPropertiesInjectFilter.class).target("http://test.com").request().get(String.class);
        Assertions.assertEquals(PROPERTY_VALUE, response);
    }
}
