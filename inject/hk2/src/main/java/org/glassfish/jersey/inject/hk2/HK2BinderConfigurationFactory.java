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

package org.glassfish.jersey.inject.hk2;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.inject.spi.BinderConfigurationFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.model.ContractProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BinderConfigurationFactory} that binds HK2 {@code AbstractBinder} as well as other
 * HK2 {@code Binder} implementation
 */
public class HK2BinderConfigurationFactory implements BinderConfigurationFactory {
    @Override
    public BinderConfiguration createBinderConfiguration(Function<Predicate<ContractProvider>, Set<Object>> getInstances) {
        return new HK2BinderConfiguration(getInstances);
    }

    private static class HK2BinderConfiguration implements BinderConfigurationFactory.BinderConfiguration {
        private final Function<Predicate<ContractProvider>, Set<Object>> getInstances;

        /**
         * A filtering strategy that includes only models that contain HK2 Binder provider contract.
         * <p>
         * This filter predicate returns {@code true} for all {@link org.glassfish.jersey.model.ContractProvider contract provider models}
         * that represent a provider registered to provide HK2 {@link org.glassfish.hk2.utilities.Binder} contract.
         * </p>
         */
        private static final Predicate<ContractProvider> BINDERS_ONLY = new Predicate<ContractProvider>() {
            @Override
            public boolean test(ContractProvider model) {
                return Binder.class.isAssignableFrom(model.getImplementationClass());
            }
        };

        private static final Function<Object, Binder> CAST_TO_BINDER = new Function<Object, Binder>() {
            @Override
            public Binder apply(final Object input) {
                return Binder.class.cast(input);
            }
        };


        private Set<Binder> configuredBinders = Collections.emptySet();

        public HK2BinderConfiguration(Function<Predicate<ContractProvider>, Set<Object>> getInstances) {
            this.getInstances = getInstances;
        }

        @Override
        public boolean configureBinders(InjectionManager injectionManager) {
            final ServiceLocator serviceLocator = getServiceLocator(injectionManager);
            if (serviceLocator != null) {
                configuredBinders = configureBinders(serviceLocator, configuredBinders);
                return !configuredBinders.isEmpty();
            }
            return false;
        }

        private Set<Binder> configureBinders(final ServiceLocator locator, final Set<Binder> configured) {
            final Set<Binder> allConfigured = Collections.newSetFromMap(new IdentityHashMap<>());
            allConfigured.addAll(configured);

            final Collection<Binder> binders = getBinders(configured);
            if (!binders.isEmpty()) {
                final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
                final DynamicConfiguration dc = dcs.createDynamicConfiguration();

                for (final Binder binder : binders) {
                    binder.bind(dc);
                    allConfigured.add(binder);
                }
                dc.commit();
            }

            return allConfigured;
        }

        private Collection<Binder> getBinders(final Set<Binder> configured) {
            return getInstances
                    .apply(BINDERS_ONLY)
                    .stream()
                    .map(CAST_TO_BINDER)
                    .filter(binder -> !configured.contains(binder))
                    .collect(Collectors.toList());
        }

        private static ServiceLocator getServiceLocator(InjectionManager injectionManager) {
            if (AbstractHk2InjectionManager.class.isInstance(injectionManager)) {
                return ((AbstractHk2InjectionManager) injectionManager).getServiceLocator();
            }
            return null;
        }
    }
}
