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
package org.glassfish.jersey.microprofile.rest.client.cdi;

import static org.glassfish.jersey.microprofile.rest.client.Constant.REST_SCOPE_FORMAT;
import static org.glassfish.jersey.microprofile.rest.client.Constant.REST_URL_FORMAT;
import org.glassfish.jersey.microprofile.rest.client.config.ConfigController;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import static org.glassfish.jersey.microprofile.rest.client.Constant.REST_URI_FORMAT;

public class RestClientProducer extends AbstractBeanProducer {

    private final Class<? extends Annotation> scope;

    public RestClientProducer(Class<?> restClient, BeanManager beanManager) {
        super(restClient, beanManager);
        this.scope = findScope();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });
        qualifiers.add(RestClient.LITERAL);
        return qualifiers;
    }

    @Override
    public Object create(CreationalContext<Object> creationalContext) {
        return RestClientBuilder
                .newBuilder()
                .baseUri(getBaseUri())
                .build(beanClass);
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {
    }

    private URI getBaseUri() {
        String uriProperty = String.format(REST_URI_FORMAT, getName());
        Optional<String> baseUri = ConfigController.getOptionalValue(uriProperty);
        if (!baseUri.isPresent()) {
            String urlProperty = String.format(REST_URL_FORMAT, getName());
            Optional<String> baseUrl = ConfigController.getOptionalValue(urlProperty);
            if (!baseUrl.isPresent()) {
                throw new IllegalArgumentException(
                        String.format("Rest Client [%s] url not found in configuration", beanClass)
                );
            } else {
                try {
                    return new URL(baseUrl.get()).toURI();
                } catch (MalformedURLException | URISyntaxException ex) {
                    throw new IllegalArgumentException(
                            String.format("Rest Client [%s] url is invalid [%s] ", beanClass, baseUri), ex
                    );
                }
            }
        } else {
            try {
                return new URI(baseUri.get());
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(
                        String.format("Rest Client [%s] uri is invalid [%s] ", beanClass, baseUri), ex
                );
            }
        }
    }

    private Class<? extends Annotation> findScope() {

        String scopeProperty = String.format(REST_SCOPE_FORMAT, getName());
        Optional<String> configuredScope = ConfigController.getOptionalValue(scopeProperty);
        if (configuredScope.isPresent()) {
            try {
                PrivilegedAction<ClassLoader> action = () -> Thread.currentThread().getContextClassLoader();
                ClassLoader classLoader = AccessController.doPrivileged(action);
                if (classLoader == null) {
                    action = () -> this.getClass().getClassLoader();
                    classLoader = AccessController.doPrivileged(action);
                }
                return (Class<? extends Annotation>) Class.forName(configuredScope.get(), true, classLoader);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(
                        String.format("Rest Client [%s] scope is invalid [%s] ", beanClass, configuredScope.get()), ex
                );
            }
        }

        List<Annotation> definedScopes = new ArrayList<>();
        Annotation[] annotations = beanClass.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (beanManager.isScope(annotation.annotationType())) {
                definedScopes.add(annotation);
            }
        }
        if (definedScopes.isEmpty()) {
            return Dependent.class;
        } else if (definedScopes.size() == 1) {
            return definedScopes.get(0).annotationType();
        } else {
            throw new IllegalArgumentException(
                    String.format("Multiple scope definition [%s] found on the Rest Client [%s]", definedScopes, beanClass)
            );
        }
    }
}
