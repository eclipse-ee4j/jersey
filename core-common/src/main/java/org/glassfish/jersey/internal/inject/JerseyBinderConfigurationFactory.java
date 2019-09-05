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

package org.glassfish.jersey.internal.inject;

import org.glassfish.jersey.inject.spi.BinderConfigurationFactory;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.model.internal.ComponentBag;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An implementation of {@link BinderConfigurationFactory} used to configure {@link Binder} instances.
 */
public class JerseyBinderConfigurationFactory implements BinderConfigurationFactory {

    @Override
    public BinderConfiguration createBinderConfiguration(Function<Predicate<ContractProvider>, Set<Object>> getInstances) {
        return new JerseyBinderConfiguration(getInstances);
    }

    private static class JerseyBinderConfiguration implements BinderConfiguration {
        private Set<Binder> configuredBinders = Collections.emptySet();
        private final Function<Predicate<ContractProvider>, Set<Object>> getInstances;
        private static final Function<Object, Binder> CAST_TO_BINDER = Binder.class::cast;

        private JerseyBinderConfiguration(Function<Predicate<ContractProvider>, Set<Object>> getInstances) {
            this.getInstances = getInstances;
        }

        @Override
        public boolean configureBinders(InjectionManager injectionManager) {
            configuredBinders = configureBinders(injectionManager, configuredBinders);
            return !configuredBinders.isEmpty();
        }

        private Set<Binder> configureBinders(InjectionManager injectionManager, Set<Binder> configured) {
            Set<Binder> allConfigured = Collections.newSetFromMap(new IdentityHashMap<>());
            allConfigured.addAll(configured);

            Collection<Binder> binders = getBinder(configured);
            if (!binders.isEmpty()) {
                injectionManager.register(CompositeBinder.wrap(binders));
                allConfigured.addAll(binders);
            }

            return allConfigured;
        }

        private Collection<Binder> getBinder(Set<Binder> configured) {
            return getInstances
                    .apply(ComponentBag.BINDERS_ONLY)
                    .stream()
                    .map(CAST_TO_BINDER)
                    .filter(binder -> !configured.contains(binder))
                    .collect(Collectors.toList());
        }
    }
}
