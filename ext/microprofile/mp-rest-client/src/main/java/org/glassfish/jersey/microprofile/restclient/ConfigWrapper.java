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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

/**
 * Configuration wrapper for {@link Configuration}. This class is needed due to custom provider registrations.
 *
 * @author David Kral
 */
class ConfigWrapper implements Configuration {

    private final Configuration jerseyBuilderConfig;
    private final Map<Class<?>, Map<Class<?>, Integer>> customProviders;

    ConfigWrapper(Configuration jerseyBuilderConfig) {
        this.jerseyBuilderConfig = jerseyBuilderConfig;
        this.customProviders = new HashMap<>();
    }

    void addCustomProvider(Class<?> provider, Map<Class<?>, Integer> contracts) {
        if (customProviders.containsKey(provider)) {
            customProviders.get(provider).putAll(contracts);
        } else {
            customProviders.put(provider, contracts);
        }
    }

    @Override
    public RuntimeType getRuntimeType() {
        return jerseyBuilderConfig.getRuntimeType();
    }

    @Override
    public Map<String, Object> getProperties() {
        return jerseyBuilderConfig.getProperties();
    }

    @Override
    public Object getProperty(String name) {
        return jerseyBuilderConfig.getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return jerseyBuilderConfig.getPropertyNames();
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return jerseyBuilderConfig.isEnabled(feature);
    }

    @Override
    public boolean isEnabled(Class<? extends Feature> featureClass) {
        return jerseyBuilderConfig.isEnabled(featureClass);
    }

    @Override
    public boolean isRegistered(Object component) {
        return jerseyBuilderConfig.isRegistered(component);
    }

    @Override
    public boolean isRegistered(Class<?> componentClass) {
        return jerseyBuilderConfig.isRegistered(componentClass);
    }

    @Override
    public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
        Map<Class<?>, Integer> map = new HashMap<>(jerseyBuilderConfig.getContracts(componentClass));
        if (customProviders.containsKey(componentClass)) {
            map.putAll(customProviders.get(componentClass));
        }
        return map;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return jerseyBuilderConfig.getClasses();
    }

    @Override
    public Set<Object> getInstances() {
        return jerseyBuilderConfig.getInstances();
    }
}
