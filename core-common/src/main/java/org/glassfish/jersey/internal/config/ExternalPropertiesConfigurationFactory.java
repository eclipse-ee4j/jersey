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

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.spi.ExternalConfigurationModel;
import org.glassfish.jersey.spi.ExternalConfigurationProvider;

import javax.annotation.Priority;
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

        final ExternalConfigurationProvider provider = mergeConfigs(EXTERNAL_CONFIGURATION_PROVIDERS);
        return provider == null ? Collections.emptyMap() : provider.getProperties();
    }


    /**
     * Input Configurable object shall be provided in order to be filled with all found properties
     *
     * @param config Input Configurable initialised object to be filled with properties
     * @return true if configured false otherwise
     */

    public static boolean configure(Configurable config) {

        if (config instanceof ExternalConfigurationModel) {
            return false; //shall not configure itself
        }

        final Map<String, Object> properties = readExternalPropertiesMap();

        properties.forEach((k, v) -> config.property(k, v));

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

            boolean config1PriorityPresent = config1.getClass().isAnnotationPresent(Priority.class);
            boolean config2PriorityPresent = config2.getClass().isAnnotationPresent(Priority.class);

            int priority1 = Priorities.USER;
            int priority2 = Priorities.USER;

            if (config1PriorityPresent) {
                priority1 = config1.getClass().getAnnotation(Priority.class).value();
            }
            if (config2PriorityPresent) {
                priority2 = config2.getClass().getAnnotation(Priority.class).value();
            }

            if (priority1 == priority2) {
                return config1.getClass().getName().compareTo(config2.getClass().getName());
            }
            return Integer.compare(priority1, priority2);
        }
    }
}