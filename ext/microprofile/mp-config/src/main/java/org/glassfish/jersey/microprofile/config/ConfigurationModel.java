/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.glassfish.jersey.spi.ExternalConfigurationModel;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Generic class which implements default properties provider's logic and wraps not used methods from ExtConfig
 *
 * @param <CONFIG> type of MP configuration impl
 */
public class ConfigurationModel<CONFIG extends Config>
        implements ExternalConfigurationModel<CONFIG> {

    private final Map<String, Object> properties;
    private final CONFIG config;

    public ConfigurationModel(CONFIG config) {
        this.properties = new HashMap<>();
        this.config = config;
    }

    @Override
    public <T> T as(String name, Class<T> clazz) {
        return config.getValue(name, clazz);
    }

    @Override
    public <T> Optional<T> getOptionalProperty(String name, Class<T> clazz) {
        return config.getOptionalValue(name, clazz);
    }

    @Override
    public CONFIG getConfig() {
        return config;
    }

    @Override
    public boolean isProperty(String name) {
        return properties.isEmpty() ? getValueFromConfig(name) != null : properties.containsKey(name);
    }

    /**
     * Allows ancestors to work with native configuration providers
     *
     * @param name property name
     * @return  property's value if any
     */
    public  Object getValueFromConfig(String name){
        return getConfig().getValue(name, Object.class);
    }

    @Override
    public RuntimeType getRuntimeType() {

        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (properties.isEmpty()) { //load properties from  external config
            final String emptyString = "";
            config.getPropertyNames().forEach(c -> {
                final String value = config.getOptionalValue(c, String.class).orElse(emptyString).trim();
                if (!value.isEmpty()) { //eliminate NULL and "" values w/o Exception
                    properties.put(c, value);
                }
            });
        }

        return properties;
    }

    @Override
    public Object getProperty(String name) {
        return properties.isEmpty() ? getValueFromConfig(name) : properties.get(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        final Set<String> names = new HashSet<>(properties.keySet());
        if (names.isEmpty()) {
            config.getPropertyNames().forEach(names::add);
        }
        return names;
    }

    public ExternalConfigurationModel mergeProperties(Map<String, Object> inputProperties) {
        if (inputProperties == null || inputProperties.isEmpty()) {
            return this;
        }
        if (properties.isEmpty()) {
            getProperties();
        }
        properties.putAll(inputProperties);

        return this;

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
