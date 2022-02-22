/*
 * Copyright (c) 2019, 2022 Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Logger;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Feature;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.spi.ExternalConfigurationModel;

/**
 * The External Configuration Model that supports {@code System} properties. The properties are listed in a property class
 * in a form of {@code public static final String} property name. The {@code String} value of the property name is searched
 * among the {@code System} properties. The property scan is performed only when
 * {@link CommonProperties#ALLOW_SYSTEM_PROPERTIES_PROVIDER} is set to {@code true}.
 */
public class SystemPropertiesConfigurationModel implements ExternalConfigurationModel<Void> {

    private static final Logger LOGGER = Logger.getLogger(SystemPropertiesConfigurationModel.class.getName());

    private static final Map<Class, Function> converters = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();
    private final AtomicBoolean gotProperties = new AtomicBoolean(false);
    private final List<String> propertyClassNames;
    static {
        converters.put(String.class, (Function<String, String>) s -> s);
        converters.put(Integer.class, (Function<String, Integer>) s -> Integer.valueOf(s));
        converters.put(Long.class, (Function<String, Long>) s -> Long.parseLong(s));
        converters.put(Boolean.class, (Function<String, Boolean>) s -> s.equalsIgnoreCase("1")
                ? true
                : Boolean.parseBoolean(s));
    }

    /**
     * Create new {@link ExternalConfigurationModel} for properties defined by classes in {@code propertyClassNames} list.
     * @param propertyClassNames List of property defining class names.
     */
    public SystemPropertiesConfigurationModel(List<String> propertyClassNames) {
        this.propertyClassNames = propertyClassNames;
    }

    protected List<String> getPropertyClassNames() {
        return propertyClassNames;
    }

    @Override
    public <T> T as(String name, Class<T> clazz) {
        if (converters.get(clazz) == null) {
            throw new IllegalArgumentException("Unsupported class type");
        }
        return (name != null && clazz != null && hasProperty(name))
                ? clazz.cast(converters.get(clazz).apply(getSystemProperty(name)))
                : null;
    }
    @Override
    public <T> Optional<T> getOptionalProperty(String name, Class<T> clazz) {
        return Optional.of(as(name, clazz));
    }

    @Override
    public ExternalConfigurationModel mergeProperties(Map<String, Object> inputProperties) {
        inputProperties.forEach((k, v) -> properties.put(k, v));
        return this;
    }

    @Override
    public Void getConfig() {
        return null;
    }

    @Override
    public boolean isProperty(String name) {
        String property = getSystemProperty(name);
        return property != null && (
                "0".equals(property) || "1".equals(property)
                        || "true".equalsIgnoreCase(property) || "false".equalsIgnoreCase(property)
        );
    }

    @Override
    public RuntimeType getRuntimeType() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        final Boolean allowSystemPropertiesProvider = as(
                CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.class
        );
        if (!Boolean.TRUE.equals(allowSystemPropertiesProvider)) {
            LOGGER.finer(LocalizationMessages.WARNING_PROPERTIES());
            return properties;
        }

        if (gotProperties.compareAndSet(false, true)) {
            try {
                AccessController.doPrivileged(PropertiesHelper.getSystemProperties())
                        .forEach((k, v) -> properties.put(String.valueOf(k), v));
            } catch (SecurityException se) {
                LOGGER.warning(LocalizationMessages.SYSTEM_PROPERTIES_WARNING());
                return getExpectedSystemProperties();
            }
        }
        return properties;
    }

    private Map<String, Object> getExpectedSystemProperties() {
        final Map<String, Object> result = new HashMap<>();
        for (String propertyClass : getPropertyClassNames()) {
            mapFieldsToProperties(result,
                    AccessController.doPrivileged(
                            ReflectionHelper.classForNamePA(propertyClass)
                    )
            );
        }

        return  result;
    }

    private static <T> void mapFieldsToProperties(Map<String, Object> properties, Class<T> clazz) {
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

    private static String getPropertyNameByField(Field field) {
        return  AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            try {
                return (String) field.get(null);
            } catch (IllegalAccessException e) {
                LOGGER.warning(e.getLocalizedMessage());
            }
            return null;
        });
    }

    private static String getSystemProperty(String name) {
        return AccessController.doPrivileged(PropertiesHelper.getSystemProperty(name));
    }

    @Override
    public Object getProperty(String name) {
        return getSystemProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return AccessController.doPrivileged(PropertiesHelper.getSystemProperties()).stringPropertyNames();
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

    // Jersey 2.x
    private boolean hasProperty(String name) {
        return getProperty(name) != null;
    }
}