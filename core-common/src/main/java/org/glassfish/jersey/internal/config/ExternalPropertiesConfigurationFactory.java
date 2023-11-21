/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.JerseyPriorities;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.spi.ExternalConfigurationModel;
import org.glassfish.jersey.spi.ExternalConfigurationProvider;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configurable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * Factory for external properties providers
 * Offers methods to work with properties loaded from providers or
 * just configure Jersey's Configurables with loaded properties from providers
 */
public class ExternalPropertiesConfigurationFactory {

    private static final List<ExternalConfigurationProvider> EXTERNAL_CONFIGURATION_PROVIDERS =
            getExternalConfigurations();


    /**
     * Map of merged properties from all found providers
     *
     * @return map of merged properties from all found/plugged providers
     */
    static Map<String, Object> readExternalPropertiesMap() {
        return readExternalPropertiesMap(EXTERNAL_CONFIGURATION_PROVIDERS);
    }

    /**
     * Map of merged properties from all given providers
     *
     * @param externalConfigProviders list of providers to use
     * @return map of merged properties from {@code externalConfigProviders} providers
     */
    private static Map<String, Object> readExternalPropertiesMap(List<ExternalConfigurationProvider> externalConfigProviders) {
        final ExternalConfigurationProvider provider = mergeConfigs(externalConfigProviders);
        return provider == null ? Collections.emptyMap() : provider.getProperties();
    }

    /**
     * Input Configurable object shall be provided in order to be filled with all found properties
     *
     * @param config Input Configurable initialised object to be filled with properties
     * @return true if configured false otherwise
     */

    public static boolean configure(Configurable config) {
        return configure((k, v) -> config.property(k, v), EXTERNAL_CONFIGURATION_PROVIDERS);
    }

    /**
     * Key Value pairs gathered by {@link ExternalConfigurationProvider}s are applied to a given {@code config}. The
     * {@code config} can be for instance {@code (k,v) -> configurable.property(k,v)} of a
     * {@link Configurable#property(String, Object) Configurable structure}, or {@code (k,v) -> properties.put(k,v)} of a
     * {@link java.util.Properties#put(Object, Object) Properties structure}.
     *
     * @param config
     * @param externalConfigurationProviders the providers to grab the properties from it.
     * @return true if configured false otherwise.
     */
    public static boolean configure(BiConsumer<String, Object> config,
                                    List<ExternalConfigurationProvider> externalConfigurationProviders) {
        if (config instanceof ExternalConfigurationModel) {
            return false; //shall not configure itself
        }

        final Map<String, Object> properties = readExternalPropertiesMap(externalConfigurationProviders);

        properties.forEach((k, v) -> config.accept(k, v));

        return true;
    }

    /**
     * Merged config model from all found configuration models
     *
     * @return merged Model object with all properties
     */
    static ExternalConfigurationModel getConfig() {
        final ExternalConfigurationProvider provider = mergeConfigs(getExternalConfigurations());
        return provider == null ? null : provider.getConfiguration();
    }

    /**
     * List of all found models as they are found by Jersey
     *
     * @return list of models (or empty list)
     */
    private static List<ExternalConfigurationProvider> getExternalConfigurations() {
        final List<ExternalConfigurationProvider> providers = new ArrayList<>();
        final ServiceFinder<ExternalConfigurationProvider> finder =
                ServiceFinder.find(ExternalConfigurationProvider.class);
        if (finder.iterator().hasNext()) {
            finder.forEach(providers::add);
        } else {
            providers.add(new SystemPropertiesConfigurationProvider());
        }
        return providers;
    }

    private static ExternalConfigurationProvider mergeConfigs(List<ExternalConfigurationProvider> configurations) {
        final Set<ExternalConfigurationProvider> orderedConfigurations = orderConfigs(configurations);
        final Iterator<ExternalConfigurationProvider> configurationIterator = orderedConfigurations.iterator();
        if (!configurationIterator.hasNext()) {
            return null;
        }
        final ExternalConfigurationProvider firstConfig = configurationIterator.next();
        while (configurationIterator.hasNext()) {
            final ExternalConfigurationProvider nextConfig = configurationIterator.next();
            firstConfig.merge(nextConfig.getConfiguration());
        }

        return firstConfig;
    }

    private static Set<ExternalConfigurationProvider> orderConfigs(List<ExternalConfigurationProvider> configurations) {

        final SortedSet<ExternalConfigurationProvider> sortedSet = new TreeSet<>(new ConfigComparator());
        sortedSet.addAll(configurations);
        return Collections.unmodifiableSortedSet(sortedSet);
    }

    private static class ConfigComparator implements Comparator<ExternalConfigurationProvider> {

        @Override
        public int compare(ExternalConfigurationProvider config1, ExternalConfigurationProvider config2) {
            int priority1 = JerseyPriorities.getPriorityValue(config1.getClass(), Priorities.USER);
            int priority2 = JerseyPriorities.getPriorityValue(config2.getClass(), Priorities.USER);

            if (priority1 == priority2) {
                return config1.getClass().getName().compareTo(config2.getClass().getName());
            }
            return Integer.compare(priority1, priority2);
        }
    }
}