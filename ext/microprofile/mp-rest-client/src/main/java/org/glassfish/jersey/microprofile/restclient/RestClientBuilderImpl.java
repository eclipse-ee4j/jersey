/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ParamConverterProvider;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.Initializable;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.ext.cdi1x.internal.CdiUtil;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;
import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Rest client builder implementation. Creates proxy instance of requested interface.
 *
 * @author David Kral
 * @author Patrik Dudits
 * @author Tomas Langer
 */
class RestClientBuilderImpl implements RestClientBuilder {

    private static final String CONFIG_DISABLE_DEFAULT_MAPPER = "microprofile.rest.client.disable.default.mapper";
    private static final String CONFIG_PROVIDERS = "/mp-rest/providers";
    private static final String CONFIG_PROVIDER_PRIORITY = "/priority";
    private static final String PROVIDER_SEPARATOR = ",";

    private final Set<ResponseExceptionMapper> responseExceptionMappers;
    private final Set<ParamConverterProvider> paramConverterProviders;
    private final List<AsyncInvocationInterceptorFactoryPriorityWrapper> asyncInterceptorFactories;
    private final Config config;
    private final ConfigWrapper configWrapper;
    private URI uri;
    private ClientBuilder clientBuilder;
    private Supplier<ExecutorService> executorService;
    private HostnameVerifier sslHostnameVerifier;
    private SSLContext sslContext;
    private KeyStore sslTrustStore;
    private KeyStore sslKeyStore;
    private char[] sslKeyStorePassword;
    private ConnectorProvider connector;

    RestClientBuilderImpl() {
        clientBuilder = ClientBuilder.newBuilder();
        responseExceptionMappers = new HashSet<>();
        paramConverterProviders = new HashSet<>();
        asyncInterceptorFactories = new ArrayList<>();
        config = ConfigProvider.getConfig();
        configWrapper = new ConfigWrapper(clientBuilder.getConfiguration());
        executorService = Executors::newCachedThreadPool;
    }

    @Override
    public RestClientBuilder baseUrl(URL url) {
        try {
            this.uri = url.toURI();
            return this;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public RestClientBuilder connectTimeout(long timeout, TimeUnit unit) {
        clientBuilder.connectTimeout(timeout, unit);
        return this;
    }

    @Override
    public RestClientBuilder readTimeout(long timeout, TimeUnit unit) {
        clientBuilder.readTimeout(timeout, unit);
        return this;
    }

    @Override
    public RestClientBuilder executorService(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("ExecutorService cannot be null.");
        }
        executorService = () -> executor;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> interfaceClass) throws IllegalStateException, RestClientDefinitionException {

        if (uri == null) {
            throw new IllegalStateException("Base uri/url cannot be null!");
        }

        for (RestClientListener restClientListener : ServiceFinder.find(RestClientListener.class)) {
            restClientListener.onNewClient(interfaceClass, this);
        }

        //Provider registration part
        processProviders(interfaceClass);
        InjectionManagerExposer injectionManagerExposer = new InjectionManagerExposer();
        register(injectionManagerExposer);

        //We need to check first if default exception mapper was not disabled by property on builder.
        registerExceptionMapper();
        //sort all AsyncInvocationInterceptorFactory by priority
        asyncInterceptorFactories.sort(Comparator.comparingInt(AsyncInvocationInterceptorFactoryPriorityWrapper::getPriority));

        clientBuilder.executorService(new ExecutorServiceWrapper(executorService.get()));

        if (null != sslContext) {
            clientBuilder.sslContext(sslContext);
        }

        if (null != sslHostnameVerifier) {
            clientBuilder.hostnameVerifier(sslHostnameVerifier);
        }

        if (null != sslTrustStore) {
            clientBuilder.trustStore(sslTrustStore);
        }

        if (null != sslKeyStore) {
            clientBuilder.keyStore(sslKeyStore, sslKeyStorePassword);
        }

        Client client;
        if (connector == null) {
            client = clientBuilder.build();
        } else {
            ClientConfig config = new ClientConfig();
            config.loadFrom(getConfiguration());
            config.connectorProvider(connector);
            client = ClientBuilder.newClient(config);
        }

        if (client instanceof Initializable) {
            ((Initializable) client).preInitialize();
        }
        WebTarget webTarget = client.target(this.uri);

        RestClientModel restClientModel = RestClientModel.from(interfaceClass,
                                                               responseExceptionMappers,
                                                               paramConverterProviders,
                                                               new ArrayList<>(asyncInterceptorFactories),
                                                               injectionManagerExposer.injectionManager,
                                                               CdiUtil.getBeanManager());

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                          new Class[] {interfaceClass, AutoCloseable.class, Closeable.class},
                                          new ProxyInvocationHandler(client, webTarget, restClientModel)
        );
    }

    @Override
    public RestClientBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    @Override
    public RestClientBuilder trustStore(KeyStore keyStore) {
        this.sslTrustStore = keyStore;
        return this;
    }

    @Override
    public RestClientBuilder keyStore(KeyStore keyStore, String password) {
        this.sslKeyStore = keyStore;
        this.sslKeyStorePassword = ((null == password) ? new char[0] : password.toCharArray());
        return this;
    }

    @Override
    public RestClientBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.sslHostnameVerifier = hostnameVerifier;
        return this;
    }

    private void registerExceptionMapper() {
        Object disableDefaultMapperJersey = clientBuilder.getConfiguration().getProperty(CONFIG_DISABLE_DEFAULT_MAPPER);
        if (disableDefaultMapperJersey != null && disableDefaultMapperJersey.equals(Boolean.FALSE)) {
            register(new DefaultResponseExceptionMapper());
        } else if (disableDefaultMapperJersey == null) {
            //If property was not set on Jersey ClientBuilder, we need to check config.
            Optional<Boolean> disableDefaultMapperConfig = config.getOptionalValue(CONFIG_DISABLE_DEFAULT_MAPPER, boolean.class);
            if (!disableDefaultMapperConfig.isPresent() || !disableDefaultMapperConfig.get()) {
                register(new DefaultResponseExceptionMapper());
            }
        }
    }

    private <T> void processProviders(Class<T> interfaceClass) {
        Object providersFromJerseyConfig = clientBuilder.getConfiguration()
                .getProperty(interfaceClass.getName() + CONFIG_PROVIDERS);
        if (providersFromJerseyConfig instanceof String && !((String) providersFromJerseyConfig).isEmpty()) {
            String[] providerArray = ((String) providersFromJerseyConfig).split(PROVIDER_SEPARATOR);
            processConfigProviders(interfaceClass, providerArray);
        }
        Optional<String> providersFromConfig = config.getOptionalValue(interfaceClass.getName() + CONFIG_PROVIDERS, String.class);
        providersFromConfig.ifPresent(providers -> {
            if (!providers.isEmpty()) {
                String[] providerArray = providersFromConfig.get().split(PROVIDER_SEPARATOR);
                processConfigProviders(interfaceClass, providerArray);
            }
        });
        RegisterProvider[] registerProviders = interfaceClass.getAnnotationsByType(RegisterProvider.class);
        for (RegisterProvider registerProvider : registerProviders) {
            register(registerProvider.value(), registerProvider.priority() < 0 ? Priorities.USER : registerProvider.priority());
        }
    }

    private void processConfigProviders(Class<?> restClientInterface, String[] providerArray) {
        for (String provider : providerArray) {
            Class<?> providerClass = AccessController.doPrivileged(ReflectionHelper.classForNamePA(provider));
            if (providerClass == null) {
                throw new IllegalStateException("No provider class with following name found: " + provider);
            }
            int priority = getProviderPriority(restClientInterface, providerClass);
            register(providerClass, priority);
        }
    }

    private int getProviderPriority(Class<?> restClientInterface, Class<?> providerClass) {
        String property = restClientInterface.getName() + CONFIG_PROVIDERS + "/"
                + providerClass.getName() + CONFIG_PROVIDER_PRIORITY;
        Object providerPriorityJersey = clientBuilder.getConfiguration().getProperty(property);
        if (providerPriorityJersey == null) {
            //If property was not set on Jersey ClientBuilder, we need to check MP config.
            Optional<Integer> providerPriorityMP = config.getOptionalValue(property, int.class);
            if (providerPriorityMP.isPresent()) {
                return providerPriorityMP.get();
            }
        } else if (providerPriorityJersey instanceof Integer) {
            return (int) providerPriorityJersey;
        }
        Priority priority = providerClass.getAnnotation(Priority.class);
        return priority == null ? -1 : priority.value();
    }

    @Override
    public Configuration getConfiguration() {
        return configWrapper;
    }

    @Override
    public RestClientBuilder property(String name, Object value) {
        clientBuilder.property(name, value);
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> componentClass) {
        if (isSupportedCustomProvider(componentClass)) {
            register(ReflectionUtil.createInstance(componentClass));
        } else {
            clientBuilder.register(componentClass);
        }
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> componentClass, int priority) {
        if (isSupportedCustomProvider(componentClass)) {
            register(ReflectionUtil.createInstance(componentClass), priority);
        } else {
            clientBuilder.register(componentClass, priority);
        }
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> componentClass, Class<?>... contracts) {
        if (isSupportedCustomProvider(componentClass)) {
            register(ReflectionUtil.createInstance(componentClass), contracts);
        } else {
            clientBuilder.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (isSupportedCustomProvider(componentClass)) {
            register(ReflectionUtil.createInstance(componentClass), contracts);
        } else {
            clientBuilder.register(componentClass, contracts);
        }
        return this;
    }

    @Override
    public RestClientBuilder register(Object component) {
        if (component instanceof ResponseExceptionMapper) {
            ResponseExceptionMapper mapper = (ResponseExceptionMapper) component;
            registerCustomProvider(component, mapper.getPriority());
            clientBuilder.register(mapper, mapper.getPriority());
        } else {
            clientBuilder.register(component);
            registerCustomProvider(component, null);
        }
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, int priority) {
        clientBuilder.register(component, priority);
        registerCustomProvider(component, priority);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, Class<?>... contracts) {
        for (Class<?> contract : contracts) {
            if (isSupportedCustomProvider(contract)) {
                register(component);
            }
        }
        clientBuilder.register(component, contracts);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, Map<Class<?>, Integer> contracts) {
        if (isSupportedCustomProvider(component.getClass())) {
            if (component instanceof ResponseExceptionMapper) {
                registerCustomProvider(component, contracts.get(ResponseExceptionMapper.class));
            } else if (component instanceof ParamConverterProvider) {
                registerCustomProvider(component, contracts.get(ParamConverterProvider.class));
            }
        }
        clientBuilder.register(component, contracts);
        return this;
    }

    private boolean isSupportedCustomProvider(Class<?> providerClass) {
        return ResponseExceptionMapper.class.isAssignableFrom(providerClass)
                || ParamConverterProvider.class.isAssignableFrom(providerClass)
                || AsyncInvocationInterceptorFactory.class.isAssignableFrom(providerClass)
                || ConnectorProvider.class.isAssignableFrom(providerClass);
    }

    private void registerCustomProvider(Object instance, Integer priority) {
        if (!isSupportedCustomProvider(instance.getClass())) {
            return;
        }
        if (instance instanceof ResponseExceptionMapper) {
            responseExceptionMappers.add((ResponseExceptionMapper) instance);
            //needs to be registered separately due to it is not possible to register custom provider in jersey
            Map<Class<?>, Integer> contracts = new HashMap<>();
            contracts.put(ResponseExceptionMapper.class, priority);
            configWrapper.addCustomProvider(instance.getClass(), contracts);
        }
        if (instance instanceof ParamConverterProvider) {
            paramConverterProviders.add((ParamConverterProvider) instance);
        }
        if (instance instanceof AsyncInvocationInterceptorFactory) {
            asyncInterceptorFactories
                    .add(new AsyncInvocationInterceptorFactoryPriorityWrapper((AsyncInvocationInterceptorFactory) instance,
                                                                              priority));
        }
        if (instance instanceof ConnectorProvider) {
            connector = (ConnectorProvider) instance;
        }
    }

    private static class InjectionManagerExposer implements Feature {
        InjectionManager injectionManager;

        @Override
        public boolean configure(FeatureContext context) {
            if (context instanceof InjectionManagerSupplier) {
                this.injectionManager = ((InjectionManagerSupplier) context).getInjectionManager();
                return true;
            } else {
                throw new IllegalArgumentException("The client needs Jersey runtime to work properly");
            }
        }
    }

    private static class AsyncInvocationInterceptorFactoryPriorityWrapper
            implements AsyncInvocationInterceptorFactory {

        private AsyncInvocationInterceptorFactory factory;
        private Integer priority;

        AsyncInvocationInterceptorFactoryPriorityWrapper(AsyncInvocationInterceptorFactory factory, Integer priority) {
            this.factory = factory;
            this.priority = priority;
        }

        @Override
        public AsyncInvocationInterceptor newInterceptor() {
            return factory.newInterceptor();
        }

        Integer getPriority() {
            if (priority == null) {
                priority = Optional.ofNullable(factory.getClass().getAnnotation(Priority.class))
                        .map(Priority::value)
                        .orElse(Priorities.USER);
            }
            return priority;
        }
    }

}
