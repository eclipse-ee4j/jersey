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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.ClientBinding;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

/**
 * Value supplier provider supporting the {@link Uri} injection annotation.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
final class WebTargetValueParamProvider extends AbstractValueParamProvider {

    private final Function<Class<? extends Configuration>, Configuration> clientConfigProvider;
    private final Supplier<Configuration> serverConfig;
    private final ConcurrentMap<BindingModel, Value<ManagedClient>> managedClients;

    private static class ManagedClient {

        private final Client instance;
        private final String customBaseUri;

        private ManagedClient(Client instance, String customBaseUri) {
            this.instance = instance;
            this.customBaseUri = customBaseUri;
        }
    }

    private static class BindingModel {

        public static final BindingModel EMPTY = new BindingModel(null);

        /**
         * Create a client binding model from a {@link ClientBinding client binding} annotation.
         *
         * @param binding client binding annotation.
         * @return binding model representing a single client binding annotation.
         */
        public static BindingModel create(Annotation binding) {
            if (binding == null || binding.annotationType().getAnnotation(ClientBinding.class) == null) {
                return EMPTY;
            } else {
                return new BindingModel(binding);
            }
        }

        /**
         * Create a client binding model from a set of {@link ClientBinding client binding}
         * annotation candidates.
         * <p>
         * A {@code ClientBinding} marker meta-annotation is used to select the set of binding
         * annotations. Only those annotations that are annotated with the binding marker
         * meta-annotation are considered as binding annotations. All other annotations are filtered
         * out and ignored.
         * </p>
         *
         * @param bindingCandidates candidate binding annotations.
         * @return composite binding representing the union of the individual binding annotations
         *         found among the binding candidates.
         */
        public static BindingModel create(final Collection<Annotation> bindingCandidates) {
            final Collection<Annotation> filtered =
                    bindingCandidates.stream()
                                     .filter(input -> input != null
                                             && input.annotationType().getAnnotation(ClientBinding.class) != null)
                                     .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                return EMPTY;
            } else if (filtered.size() > 1) {
                throw new ProcessingException("Too many client binding annotations.");
            } else {
                return new BindingModel(filtered.iterator().next());
            }
        }

        private final Annotation annotation;
        private final Class<? extends Configuration> configClass;
        private final boolean inheritProviders;
        private final String baseUri;

        private BindingModel(Annotation annotation) {
            if (annotation == null) {
                this.annotation = null;
                this.configClass = ClientConfig.class;
                this.inheritProviders = true;
                this.baseUri = "";
            } else {
                this.annotation = annotation;
                final ClientBinding cba = annotation.annotationType().getAnnotation(ClientBinding.class);
                this.configClass = cba.configClass();
                this.inheritProviders = cba.inheritServerProviders();
                this.baseUri = cba.baseUri();
            }
        }

        /**
         * Get the client binding annotation this model represents.
         *
         * @return client binding annotation.
         */
        public Annotation getAnnotation() {
            return annotation;
        }

        /**
         * Get the configuration class to be used.
         *
         * @return client configuration class to be used.
         */
        public Class<? extends Configuration> getConfigClass() {
            return configClass;
        }

        /**
         * Check if the server-side providers should be inherited.
         *
         * @return {@code true} if server-side providers should be inherited, {@code false} otherwise.
         */
        public boolean inheritProviders() {
            return inheritProviders;
        }

        /**
         * Get the client base URI.
         *
         * @return client base URI.
         */
        public String baseUri() {
            return baseUri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BindingModel that = (BindingModel) o;
            return annotation != null ? annotation.equals(that.annotation) : that.annotation == null;
        }

        @Override
        public int hashCode() {
            return annotation != null ? annotation.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "BindingModel{"
                    + "binding=" + annotation
                    + ", configClass=" + configClass
                    + ", inheritProviders=" + inheritProviders
                    + ", baseUri=" + baseUri
                    + '}';
        }
    }

    private static final class WebTargetValueSupplier implements Function<ContainerRequest, WebTarget> {

        private final String uri;
        private final Value<ManagedClient> client;

        WebTargetValueSupplier(String uri, Value<ManagedClient> client) {
            this.uri = uri;
            this.client = client;
        }

        @Override
        public WebTarget apply(ContainerRequest containerRequest) {
            // no need for try-catch - unlike for @*Param annotations, any issues with @Uri would usually be caused
            // by incorrect server code, so the default runtime exception mapping to 500 is appropriate
            final ExtendedUriInfo uriInfo = containerRequest.getUriInfo();

            final Map<String, Object> pathParamValues =
                    uriInfo.getPathParameters().entrySet()
                           .stream()
                           .collect(Collectors.toMap(
                                   Map.Entry::getKey,
                                   (Function<Map.Entry<String, List<String>>, Object>) stringObjectEntry -> {
                                       List<String> input = stringObjectEntry.getValue();
                                       return input.isEmpty() ? null : input.get(0);
                                   }));

            JerseyUriBuilder uriBuilder = new JerseyUriBuilder().uri(this.uri).resolveTemplates(pathParamValues);

            final ManagedClient managedClient = client.get();

            if (!uriBuilder.isAbsolute()) {
                final String customBaseUri = managedClient.customBaseUri;
                final String rootUri = customBaseUri.isEmpty() ? uriInfo.getBaseUri().toString() : customBaseUri;

                uriBuilder = new JerseyUriBuilder().uri(rootUri).path(uriBuilder.toTemplate());
            }

            return managedClient.instance.target(uriBuilder);
        }
    }

    /**
     * Initialize the provider.
     *
     * @param serverConfig        server-side serverConfig.
     * @param clientConfigProvider function which get or create a new client serverConfig according to provided class.
     */
    public WebTargetValueParamProvider(Supplier<Configuration> serverConfig,
            Function<Class<? extends Configuration>, Configuration> clientConfigProvider) {
        super(null, Parameter.Source.URI);
        this.clientConfigProvider = clientConfigProvider;
        this.serverConfig = serverConfig;

        this.managedClients = new ConcurrentHashMap<>();
        // init default client
        this.managedClients.put(BindingModel.EMPTY, Values.lazy(new Value<ManagedClient>() {
            @Override
            public ManagedClient get() {
                final Client client;
                if (serverConfig.get() == null) {
                    client = ClientBuilder.newClient();
                } else {
                    ClientConfig clientConfig = new ClientConfig();
                    copyProviders(serverConfig.get(), clientConfig);
                    client = ClientBuilder.newClient(clientConfig);
                }
                return new ManagedClient(client, "");
            }
        }));
    }

    private void copyProviders(Configuration source, Configurable<?> target) {
        final Configuration targetConfig = target.getConfiguration();
        for (Class<?> c : source.getClasses()) {
            if (!targetConfig.isRegistered(c)) {
                target.register(c, source.getContracts(c));
            }
        }

        for (Object o : source.getInstances()) {
            Class<?> c = o.getClass();
            if (!targetConfig.isRegistered(o)) {
                target.register(c, source.getContracts(c));
            }
        }
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(final Parameter parameter) {
        return Errors.processWithException(new Producer<Function<ContainerRequest, ?>>() {

            @Override
            public Function<ContainerRequest, ?> call() {
                String targetUriTemplate = parameter.getSourceName();
                if (targetUriTemplate == null || targetUriTemplate.length() == 0) {
                    // Invalid URI parameter name
                    Errors.warning(this, LocalizationMessages.INJECTED_WEBTARGET_URI_INVALID(targetUriTemplate));
                    return null;
                }

                final Class<?> rawParameterType = parameter.getRawType();
                if (rawParameterType == WebTarget.class) {
                    final BindingModel binding = BindingModel.create(Arrays.<Annotation>asList(parameter.getAnnotations()));

                    Value<ManagedClient> client = managedClients.get(binding);
                    if (client == null) {
                        client = Values.lazy(new Value<ManagedClient>() {
                            @Override
                            public ManagedClient get() {
                                final String prefix = binding.getAnnotation().annotationType().getName() + ".";
                                final String baseUriProperty = prefix + "baseUri";
                                final Object bu = serverConfig.get().getProperty(baseUriProperty);
                                final String customBaseUri = (bu != null) ? bu.toString() : binding.baseUri();

                                final String configClassProperty = prefix + "configClass";
                                final ClientConfig cfg = resolveConfig(configClassProperty, binding);

                                final String inheritProvidersProperty = prefix + "inheritServerProviders";
                                if (PropertiesHelper.isProperty(serverConfig.get().getProperty(inheritProvidersProperty))
                                        || binding.inheritProviders()) {
                                    copyProviders(serverConfig.get(), cfg);
                                }

                                final String propertyPrefix = prefix + "property.";
                                Collection<String> clientProperties =
                                        serverConfig.get().getPropertyNames()
                                                    .stream()
                                                    .filter(property -> property.startsWith(propertyPrefix))
                                                    .collect(Collectors.toSet());

                                for (String property : clientProperties) {
                                    cfg.property(property.substring(propertyPrefix.length()),
                                            serverConfig.get().getProperty(property));
                                }

                                return new ManagedClient(ClientBuilder.newClient(cfg), customBaseUri);
                            }
                        });
                        final Value<ManagedClient> previous = managedClients.putIfAbsent(binding, client);
                        if (previous != null) {
                            client = previous;
                        }
                    }
                    return new WebTargetValueSupplier(targetUriTemplate, client);
                } else {
                    Errors.warning(this, LocalizationMessages.UNSUPPORTED_URI_INJECTION_TYPE(rawParameterType));
                    return null;
                }
            }
        });
    }

    private ClientConfig resolveConfig(final String configClassProperty, final BindingModel binding) {
        Class<? extends Configuration> configClass = binding.getConfigClass();
        final Object _cc = serverConfig.get().getProperty(configClassProperty);
        if (_cc != null) {
            Class<?> cc;
            if (_cc instanceof String) {
                cc = AccessController.doPrivileged(ReflectionHelper.classForNamePA((String) _cc));
            } else if (_cc instanceof Class) {
                cc = (Class<?>) _cc;
            } else {
                cc = null; // will cause a warning
            }

            if (cc != null && Configuration.class.isAssignableFrom(cc)) {
                configClass = cc.asSubclass(Configuration.class);
            } else {
                Errors.warning(this, LocalizationMessages.ILLEGAL_CLIENT_CONFIG_CLASS_PROPERTY_VALUE(
                        configClassProperty,
                        _cc,
                        configClass.getName()
                ));
            }
        }

        final Configuration cfg = clientConfigProvider.apply(configClass);

        return (cfg instanceof ClientConfig) ? (ClientConfig) cfg : new ClientConfig().loadFrom(cfg);
    }

}
