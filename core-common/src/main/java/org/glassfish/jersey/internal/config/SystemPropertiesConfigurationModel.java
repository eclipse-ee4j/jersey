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

package org.glassfish.jersey.internal.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.spi.ExternalConfigurationModel;


class SystemPropertiesConfigurationModel implements ExternalConfigurationModel<Void> {

    private static final Logger log = Logger.getLogger(SystemPropertiesConfigurationModel.class.getName());
    static final List<String> PROPERTY_CLASSES = Arrays.asList(
            "org.glassfish.jersey.server.ServerProperties",
            "org.glassfish.jersey.client.ClientProperties",
            "org.glassfish.jersey.servlet.ServletProperties",
            "org.glassfish.jersey.message.MessageProperties",
            "org.glassfish.jersey.apache.connector.ApacheClientProperties",
            "org.glassfish.jersey.jdk.connector.JdkConnectorProperties",
            "org.glassfish.jersey.jetty.connector.JettyClientProperties",
            "org.glassfish.jersey.media.multipart.MultiPartProperties",
            "org.glassfish.jersey.server.oauth1.OAuth1ServerProperties");


    private static final Map<Class, Function> converters = new HashMap<>();
    static {
        converters.put(String.class, (Function<String, String>) s -> s);
        converters.put(Integer.class, (Function<String, Integer>) s -> Integer.valueOf(s));
        converters.put(Boolean.class, (Function<String, Boolean>) s -> s.equalsIgnoreCase("1")
                ? true
                : Boolean.parseBoolean(s));
    }

    private String getSystemProperty(String name) {
        return AccessController.doPrivileged(PropertiesHelper.getSystemProperty(name));
    }

    @Override
    public <T> T as(String name, Class<T> clazz) {
        if (converters.get(clazz) == null) {
            throw new IllegalArgumentException("Unsupported class type");
        }
        return (name != null && clazz != null && isProperty(name))
                ? clazz.cast(converters.get(clazz).apply(getSystemProperty(name)))
                : null;
    }



    @Override
    public <T> Optional<T> getOptionalProperty(String name, Class<T> clazz) {
        return Optional.of(as(name, clazz));
    }

    @Override
    public ExternalConfigurationModel mergeProperties(Map<String, Object> inputProperties) {
        return this;
    }

    @Override
    public Void getConfig() {
        return null;
    }

    @Override
    public boolean isProperty(String name) {
        return Optional.ofNullable(
                AccessController.doPrivileged(
                        PropertiesHelper.getSystemProperty(name)
                )
        ).isPresent();
    }

    @Override
    public RuntimeType getRuntimeType() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> result = new HashMap<>();

        final Boolean allowSystemPropertiesProvider = as(
                CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.class
        );
        if (!Boolean.TRUE.equals(allowSystemPropertiesProvider)) {
            log.finer(LocalizationMessages.WARNING_PROPERTIES());
            return result;
        }

        try {
            AccessController.doPrivileged(PropertiesHelper.getSystemProperties())
                    .forEach((k, v) -> result.put(String.valueOf(k), v));
        } catch (SecurityException se) {
            log.warning(LocalizationMessages.SYSTEM_PROPERTIES_WARNING());
            return getExpectedSystemProperties();
        }
        return result;
    }

    private Map<String, Object> getExpectedSystemProperties() {
        final Map<String, Object> result = new HashMap<>();
        mapFieldsToProperties(result, CommonProperties.class);
        for (String propertyClass : PROPERTY_CLASSES) {
            mapFieldsToProperties(result,
                    AccessController.doPrivileged(
                            ReflectionHelper.classForNamePA(propertyClass)
                    )
            );
        }

        return  result;
    }

    private <T> void mapFieldsToProperties(Map<String, Object> properties, Class<T> clazz) {
        if (clazz == null) {
            return;
        }

        final Field[] fields = AccessController.doPrivileged(
                ReflectionHelper.getDeclaredFieldsPA(clazz)
        );

        for (final Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(String.class)) {
                final String propertyValue = getPropertyNameByField(field);
                if (propertyValue != null) {
                    String value = getSystemProperty(propertyValue);
                    if (value != null) {
                        properties.put(propertyValue, value);
                    }
                }
            }
        }
    }

    private String getPropertyNameByField(Field field) {
        return  AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            try {
                return (String) field.get(null);
            } catch (IllegalAccessException e) {
                log.warning(e.getLocalizedMessage());
            }
            return null;
        });
    }

    @Override
    public Object getProperty(String name) {
        return getSystemProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return PropertiesHelper.getSystemProperties().run().stringPropertyNames();
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return false;
    }

    @Override
    public boolean isEnabled(Class<? extends Feature> featureClass) {
        return false;
    }

    @Override
    public boolean isRegistered(Object component) {
        return false;
    }

    @Override
    public boolean isRegistered(Class<?> componentClass) {
        return false;
    }

    @Override
    public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
        return null;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return null;
    }

    @Override
    public Set<Object> getInstances() {
        return null;
    }
}