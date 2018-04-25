/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.model;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

/**
 * Jersey contract provider model.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ContractProvider implements Scoped, NameBound {
    /**
     * "No priority" constant.
     */
    public static final int NO_PRIORITY = -1;

    /**
     * Create new contract provider model builder.
     *
     * @param implementationClass class which the contracts belong to.
     * @return new contract provider builder.
     */
    public static Builder builder(Class<?> implementationClass) {
        return new Builder(implementationClass);
    }

    /**
     * Create new contract provider model builder from an existing one.
     *
     * @param original existing contract provider model.
     * @return new contract provider builder.
     */
    public static Builder builder(final ContractProvider original) {
        return new Builder(original);
    }

    /**
     * Contract provider model builder.
     */
    public static final class Builder {

        private static final ContractProvider EMPTY_MODEL =
                new ContractProvider(null, Singleton.class, Collections.emptyMap(), NO_PRIORITY, Collections.emptySet());

        private Class<?> implementationClass = null;
        private Class<? extends Annotation> scope = null;
        private Map<Class<?>, Integer> contracts = new HashMap<>();
        private int defaultPriority = NO_PRIORITY;
        private Set<Class<? extends Annotation>> nameBindings = Collections.newSetFromMap(new IdentityHashMap<>());

        private Builder(Class<?> implementationClass) {
            this.implementationClass = implementationClass;
        }

        private Builder(final ContractProvider original) {
            this.implementationClass = original.implementationClass;
            this.scope = original.scope;
            this.contracts.putAll(original.contracts);
            this.defaultPriority = original.defaultPriority;
            this.nameBindings.addAll(original.nameBindings);
        }

        /**
         * Change contract provider scope. (Default scope is {@link Singleton}.)
         *
         * @param scope contract provider scope.
         * @return updated builder.
         */
        public Builder scope(final Class<? extends Annotation> scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Add a new provided contract.
         *
         * @param contract additional provided contract.
         * @return updated builder.
         */
        public Builder addContract(final Class<?> contract) {
            return addContract(contract, defaultPriority);
        }

        /**
         * Add a new provided contract with priority.
         *
         * @param contract additional provided contract.
         * @param priority priority for the contract.
         * @return updated builder.
         */
        public Builder addContract(final Class<?> contract, final int priority) {
            contracts.put(contract, priority);
            return this;
        }

        /**
         * Add a new provided contracts.
         *
         * @param contracts additional provided contracts.
         * @return updated builder.
         */
        public Builder addContracts(final Map<Class<?>, Integer> contracts) {
            this.contracts.putAll(contracts);
            return this;
        }

        /**
         * Add a new provided contracts.
         *
         * @param contracts additional provided contracts.
         * @return updated builder.
         */
        public Builder addContracts(final Collection<Class<?>> contracts) {
            for (final Class<?> contract : contracts) {
                addContract(contract, defaultPriority);
            }
            return this;
        }

        /**
         * Set the contract default provider priority. (Default value is {@link ContractProvider#NO_PRIORITY})
         *
         * @param defaultPriority default contract provider priority.
         * @return updated builder.
         */
        public Builder defaultPriority(final int defaultPriority) {
            this.defaultPriority = defaultPriority;
            return this;
        }

        /**
         * Add a new contract provider name binding.
         *
         * @param binding name binding.
         * @return updated builder.
         */
        public Builder addNameBinding(final Class<? extends Annotation> binding) {
            this.nameBindings.add(binding);
            return this;
        }

        /**
         * Get the scope of the built contract provider model.
         *
         * @return scope associated with the model or {@code null} if no scope
         * has been set explicitly.
         */
        public Class<? extends Annotation> getScope() {
            return scope;
        }

        /**
         * Get the map of contracts for the built contract provider model.
         *
         * @return contracts associated with the model.
         */
        public Map<Class<?>, Integer> getContracts() {
            return contracts;
        }

        /**
         * Get the default priority of the built contract provider model.
         *
         * @return default priority associated with the model.
         */
        public int getDefaultPriority() {
            return defaultPriority;
        }

        /**
         * Get name bindings of the built contract provider model.
         *
         * @return name bindings associated with the model.
         */
        public Set<Class<? extends Annotation>> getNameBindings() {
            return nameBindings;
        }

        /**
         * Build a new contract provider model.
         *
         * @return new contract provider model.
         */
        public ContractProvider build() {
            if (scope == null) {
                scope = Singleton.class;
            }

            final Map<Class<?>, Integer> _contracts = (contracts.isEmpty())
                    ? Collections.emptyMap()
                    : contracts.entrySet()
                               .stream()
                               .collect(Collectors.toMap((Function<Map.Entry<Class<?>, Integer>, Class<?>>) Map.Entry::getKey,
                                                         classIntegerEntry -> {
                                                             Integer priority = classIntegerEntry.getValue();
                                                             return (priority != NO_PRIORITY) ? priority : defaultPriority;
                                                         }));

            final Set<Class<? extends Annotation>> bindings = (nameBindings.isEmpty())
                    ? Collections.emptySet() : Collections.unmodifiableSet(nameBindings);

            if (implementationClass == null && scope == Singleton.class && _contracts.isEmpty() && defaultPriority == NO_PRIORITY
                    && bindings.isEmpty()) {
                return EMPTY_MODEL;
            }

            return new ContractProvider(implementationClass, scope, _contracts, defaultPriority, bindings);
        }
    }

    private final Class<?> implementationClass;
    private final Map<Class<?>, Integer> contracts;
    private final int defaultPriority;
    private final Set<Class<? extends Annotation>> nameBindings;
    private final Class<? extends Annotation> scope;

    private ContractProvider(
            final Class<?> implementationClass,
            final Class<? extends Annotation> scope,
            final Map<Class<?>, Integer> contracts,
            final int defaultPriority,
            final Set<Class<? extends Annotation>> nameBindings) {

        this.implementationClass = implementationClass;
        this.scope = scope;
        this.contracts = contracts;
        this.defaultPriority = defaultPriority;
        this.nameBindings = nameBindings;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    /**
     * Get the implementation class which the contracts belong to.
     *
     * @return implementation class.
     */
    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    /**
     * Get provided contracts recognized by Jersey.
     *
     * @return provided contracts.
     *
     * @see org.glassfish.jersey.spi.Contract
     */
    public Set<Class<?>> getContracts() {
        return contracts.keySet();
    }

    /**
     * Get the map of contracts and their priorities.
     *
     * @return contracts and their priorities.
     */
    public Map<Class<?>, Integer> getContractMap() {
        return contracts;
    }

    @Override
    public boolean isNameBound() {
        return !nameBindings.isEmpty();
    }

    /**
     * Get the provider contract priority, if set, default component provider, if not set.
     *
     * @param contract provider contract.
     * @return provider priority.
     *
     * @see javax.annotation.Priority
     */
    public int getPriority(final Class<?> contract) {
        if (contracts.containsKey(contract)) {
            return contracts.get(contract);
        }
        return defaultPriority;
    }

    @Override
    public Set<Class<? extends Annotation>> getNameBindings() {
        return nameBindings;
    }
}
