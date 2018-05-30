/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.rest.client;

import org.glassfish.jersey.microprofile.rest.client.ext.DefaultResponseExceptionMapper;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import static org.glassfish.jersey.microprofile.rest.client.Constant.DISABLE_DEFAULT_EXCEPTION_MAPPER;
import org.glassfish.jersey.microprofile.rest.client.config.ConfigController;
import static java.lang.Boolean.FALSE;
import java.net.URI;
import java.util.concurrent.ExecutorService;

public class RestClientBuilderImpl implements RestClientBuilder {

    private URI baseUri;

    private final ClientBuilder clientBuilder;

    public RestClientBuilderImpl() {
        clientBuilder = ClientBuilder.newBuilder();
    }

    @Override
    public RestClientBuilder baseUrl(URL url) {
        try {
            this.baseUri = url.toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(
                    String.format("Rest Client url is invalid [%s] ", url), ex
            );
        }
        return this;
    }

    @Override
    public RestClientBuilder baseUri(URI uri) {
        this.baseUri = uri;
        return this;
    }

    @Override
    public RestClientBuilder executorService(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("ExecutorService is null");
        }
        clientBuilder.executorService(executor);
        return this;
    }

    @Override
    public <T> T build(Class<T> restClientInterface) throws IllegalStateException, RestClientDefinitionException {
        if (baseUri == null) {
            throw new IllegalStateException("Base URI or URL can't be null");
        }

        // interface validity
        RestClientValidator.getInstance().validate(restClientInterface);

        registerDefaultExceptionMapper();
        registerProviders(restClientInterface);

        Client client =  clientBuilder.build();
        WebTarget webTarget = client.target(baseUri);
        return WebResourceFactory.newResource(restClientInterface, webTarget);
    }

    private void registerDefaultExceptionMapper() {
        // Default exception mapper check per client basis
        Object disableDefaultExceptionMapperProp = getConfiguration()
                .getProperty(DISABLE_DEFAULT_EXCEPTION_MAPPER);
        if (disableDefaultExceptionMapperProp == null) {
            //check MicroProfile Config
            boolean disableDefaultExceptionMapper = ConfigController
                    .getOptionalValue(DISABLE_DEFAULT_EXCEPTION_MAPPER, Boolean.class)
                    .orElse(FALSE);
            if (!disableDefaultExceptionMapper) {
                register(DefaultResponseExceptionMapper.class);
            }
        } else if (FALSE.equals(disableDefaultExceptionMapperProp)) {
            register(DefaultResponseExceptionMapper.class);
        }
    }

    private <T> void registerProviders(Class<T> restClient) {
        RegisterProvider[] providers = restClient.getAnnotationsByType(RegisterProvider.class);
        for (RegisterProvider provider : providers) {
            register(provider.value(), provider.priority());
        }
    }

    @Override
    public Configuration getConfiguration() {
        return clientBuilder.getConfiguration();
    }

    @Override
    public RestClientBuilder property(String name, Object value) {
        clientBuilder.property(name, value);
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> componentClass) {
        clientBuilder.register(componentClass);
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> type, int priority) {
        clientBuilder.register(type, priority);
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> type, Class<?>... contracts) {
        clientBuilder.register(type, contracts);
        return this;
    }

    @Override
    public RestClientBuilder register(Class<?> type, Map<Class<?>, Integer> contracts) {
        clientBuilder.register(type, contracts);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component) {
        clientBuilder.register(component);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, int priority) {
        clientBuilder.register(component, priority);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, Class<?>... contracts) {
        clientBuilder.register(component, contracts);
        return this;
    }

    @Override
    public RestClientBuilder register(Object component, Map<Class<?>, Integer> contracts) {
        clientBuilder.register(component, contracts);
        return this;
    }

}
