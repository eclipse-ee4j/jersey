/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.net.ssl.HostnameVerifier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Handles proper rest client injection.
 *
 * Contains information about the rest client interface and extracts additional parameters from
 * config.
 *
 * @author David Kral
 * @author Tomas Langer
 */
class RestClientProducer implements Bean<Object>, PassivationCapable {

    private static final String CONFIG_URL = "/mp-rest/url";
    private static final String CONFIG_URI = "/mp-rest/uri";
    private static final String CONFIG_SCOPE = "/mp-rest/scope";
    private static final String CONFIG_CONNECTION_TIMEOUT = "/mp-rest/connectTimeout";
    private static final String CONFIG_READ_TIMEOUT = "/mp-rest/readTimeout";
    private static final String CONFIG_SSL_TRUST_STORE_LOCATION = "/mp-rest/trustStore";
    private static final String CONFIG_SSL_TRUST_STORE_TYPE = "/mp-rest/trustStoreType";
    private static final String CONFIG_SSL_TRUST_STORE_PASSWORD = "/mp-rest/trustStorePassword";
    private static final String CONFIG_SSL_KEY_STORE_LOCATION = "/mp-rest/keyStore";
    private static final String CONFIG_SSL_KEY_STORE_TYPE = "/mp-rest/keyStoreType";
    private static final String CONFIG_SSL_KEY_STORE_PASSWORD = "/mp-rest/keyStorePassword";
    private static final String CONFIG_SSL_HOSTNAME_VERIFIER = "/mp-rest/hostnameVerifier";
    private static final String CONFIG_PROVIDERS = "/mp-rest/providers";
    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    private static final String CLASSPATH_LOCATION = "classpath:";
    private static final String FILE_LOCATION = "file:";

    private final Class<?> interfaceType;
    private final Config config;
    private final String fqcn;
    private final Optional<RegisterRestClient> restClientAnnotation;
    private final Optional<String> configKey;
    private final Class<? extends Annotation> scope;

    /**
     * Creates new instance of RestClientProducer.
     *
     * @param interfaceType rest client interface
     * @param beanManager   bean manager
     */
    RestClientProducer(Class<?> interfaceType, BeanManager beanManager) {
        this.interfaceType = interfaceType;
        this.config = ConfigProvider.getConfig();
        this.fqcn = interfaceType.getName();
        this.restClientAnnotation = Optional.ofNullable(interfaceType.getAnnotation(RegisterRestClient.class));
        this.configKey = restClientAnnotation.map(RegisterRestClient::configKey);
        this.scope = resolveClientScope(interfaceType, beanManager, config, fqcn, configKey);
    }

    @Override
    public Class<?> getBeanClass() {
        return interfaceType;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(CreationalContext<Object> creationalContext) {
        // Base URL
        RestClientBuilder restClientBuilder = RestClientBuilder.newBuilder().baseUrl(getBaseUrl());
        // Connection timeout (if configured)
        getConfigOption(Long.class, CONFIG_CONNECTION_TIMEOUT)
                .ifPresent(aLong -> restClientBuilder.connectTimeout(aLong, TimeUnit.MILLISECONDS));
        // Connection read timeout (if configured)
        getConfigOption(Long.class, CONFIG_READ_TIMEOUT)
                .ifPresent(aLong -> restClientBuilder.readTimeout(aLong, TimeUnit.MILLISECONDS));

        // Providers from configuration
        addConfiguredProviders(restClientBuilder);

        // SSL configuration
        getHostnameVerifier()
                .ifPresent(restClientBuilder::hostnameVerifier);
        getKeyStore(CONFIG_SSL_KEY_STORE_LOCATION, CONFIG_SSL_KEY_STORE_TYPE, CONFIG_SSL_KEY_STORE_PASSWORD)
                .ifPresent(keyStore -> restClientBuilder.keyStore(keyStore.keyStore, keyStore.password));
        getKeyStore(CONFIG_SSL_TRUST_STORE_LOCATION, CONFIG_SSL_TRUST_STORE_TYPE, CONFIG_SSL_TRUST_STORE_PASSWORD)
                .ifPresent(keystore -> restClientBuilder.trustStore(keystore.keyStore));

        return restClientBuilder.build(interfaceType);
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(interfaceType);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(new AnnotationLiteral<Default>() { });
        annotations.add(new AnnotationLiteral<Any>() { });
        annotations.add(RestClient.LITERAL);
        return annotations;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return interfaceType.getName() + "RestClient";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String toString() {
        return "RestClientProducer [ interfaceType: " + interfaceType.getSimpleName()
                + " ] with Qualifiers [" + getQualifiers() + "]";
    }

    @Override
    public String getId() {
        return interfaceType.getName();
    }

    private void addConfiguredProviders(RestClientBuilder restClientBuilder) {
        Optional<String[]> configOption = getConfigOption(String[].class, CONFIG_PROVIDERS);
        if (!configOption.isPresent()) {
            return;
        }

        String[] classNames = configOption.get();
        for (String className : classNames) {
            Class<?> providerClass = AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
            Optional<Integer> priority = getConfigOption(Integer.class, CONFIG_PROVIDERS + "/"
                    + className
                    + "/priority");

            if (priority.isPresent()) {
                restClientBuilder.register(providerClass, priority.get());
            } else {
                restClientBuilder.register(providerClass);
            }
        }
    }

    private URL getBaseUrl() {
        Supplier<String> baseUrlDefault = () -> {
            throw new DeploymentException("This interface has to be annotated with @RegisterRestClient annotation.");
        };

        String baseUrl = getOption(config,
                                   fqcn,
                                   configKey,
                                   restClientAnnotation.map(RegisterRestClient::baseUri),
                                   baseUrlDefault,
                                   String.class,
                                   CONFIG_URI,
                                   CONFIG_URL);

        try {
            return new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("URL is not in valid format for Rest interface " + interfaceType.getName()
                                                    + ": " + baseUrl, e);
        }
    }

    // a helper to get a long option from configuration based on fully qualified class name or config key
    private <T> Optional<T> getConfigOption(Class<T> optionType, String propertySuffix) {
        return Optional.ofNullable(getOption(config,
                                             fqcn,
                                             configKey,
                                             Optional.empty(),
                                             () -> null,
                                             optionType,
                                             propertySuffix));
    }

    // a helper to find an option from configuration based on fully qualified class name or config key, from annotation,
    // or using a default value
    private static <T> T getOption(Config config,
                                   String fqcn,
                                   Optional<String> configKey,
                                   Optional<T> valueFromAnnotation,
                                   Supplier<T> defaultValue,
                                   Class<T> propertyType,
                                   String... propertySuffixes) {

        /*
         * Spec:
         *  1. if explicit configuration for class exists, use it
         *  2. if explicit configuration for config key exists, use it
         *  3. if annotated and explicitly configured, use it
         *  4. use default
         */

        // configuration for fully qualified class name
        for (String propertySuffix : propertySuffixes) {
            // 1.
            Optional<T> value = config.getOptionalValue(fqcn + propertySuffix, propertyType);
            if (value.isPresent()) {
                return value.get();
            }
        }

        // configuration for config key
        if (configKey.isPresent()) {
            String theKey = configKey.get();
            if (!theKey.isEmpty()) {
                for (String propertySuffix : propertySuffixes) {
                    // 2.
                    Optional<T> value = config.getOptionalValue(theKey + propertySuffix, propertyType);
                    if (value.isPresent()) {
                        return value.get();
                    }
                }
            }
        }

        // 3. and 4.
        return valueFromAnnotation.orElseGet(defaultValue);
    }

    private Optional<KeyStoreConfig> getKeyStore(String configLocation, String configType, String configPassword) {
        String keyStoreLocation = getConfigOption(String.class, configLocation).orElse(null);
        if (keyStoreLocation == null) {
            return Optional.empty();
        }

        String keyStoreType = getConfigOption(String.class, configType).orElse(DEFAULT_KEYSTORE_TYPE);
        String password = getConfigOption(String.class, configPassword).orElse(null);

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to create keystore of type: " + keyStoreType + " for " + interfaceType, e);
        }

        try (InputStream storeStream = locationToStream(keyStoreLocation)) {
            keyStore.load(storeStream, password.toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException("Failed to load keystore from " + keyStoreLocation, e);
        }

        return Optional.of(new KeyStoreConfig(keyStore, password));
    }

    private InputStream locationToStream(String location) throws IOException {
        // location in config has two flavors:
        // file:/home/user/some.jks
        // classpath:/client-keystore.jks

        if (location.startsWith(CLASSPATH_LOCATION)) {
            String resource = location.substring(CLASSPATH_LOCATION.length());
            // first try to read from the same classloader as the rest client interface
            InputStream result = interfaceType.getResourceAsStream(resource);
            if (null == result) {
                // and if not found, use the context classloader (for example in TCK, this is needed)
                result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                if (result == null && resource.startsWith("/")) {
                    result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource.substring(1));
                }
            }
            return result;
        } else if (location.startsWith(FILE_LOCATION)) {
            return Files.newInputStream(Paths.get(URI.create(location)));
        } else {
            throw new IllegalStateException("Location of keystore must start with either classpath: or file:, but is: "
                                                    + location
                                                    + " for "
                                                    + interfaceType);
        }
    }

    private Optional<HostnameVerifier> getHostnameVerifier() {
        Optional<String> verifier = getConfigOption(String.class, CONFIG_SSL_HOSTNAME_VERIFIER);

        return verifier.map(className -> {
            Class<? extends HostnameVerifier> theClass =
                    AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
            if (theClass == null) {
                throw new IllegalStateException("Invalid hostname verifier class: " + className);
            }

            return ReflectionUtil.createInstance(theClass);
        });
    }

    private static Class<? extends Annotation> resolveClientScope(Class<?> interfaceType,
                                                                  BeanManager beanManager,
                                                                  Config config,
                                                                  String fqcn,
                                                                  Optional<String> configKey) {

        String configuredScope = getOption(config,
                                           fqcn,
                                           configKey,
                                           Optional.empty(),
                                           () -> null,
                                           String.class,
                                           CONFIG_SCOPE);

        if (configuredScope != null) {
            Class<Annotation> scope = AccessController.doPrivileged(ReflectionHelper.classForNamePA(configuredScope));
            if (scope == null) {
                throw new IllegalStateException("Invalid scope from config: " + configuredScope);
            }
            return scope;
        }

        List<Annotation> possibleScopes = Arrays.stream(interfaceType.getDeclaredAnnotations())
                .filter(annotation -> beanManager.isScope(annotation.annotationType()))
                .collect(Collectors.toList());

        if (possibleScopes.size() == 1) {
            return possibleScopes.get(0).annotationType();
        } else if (possibleScopes.isEmpty()) {
            return Dependent.class;
        } else {
            throw new IllegalArgumentException("Client should have only one scope defined: "
                                                       + interfaceType + " has " + possibleScopes);
        }
    }

    private static final class KeyStoreConfig {
        private final KeyStore keyStore;
        private final String password;

        private KeyStoreConfig(KeyStore keyStore, String password) {
            this.keyStore = keyStore;
            this.password = password;
        }
    }
}
