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

package org.glassfish.jersey.model.internal;

import java.lang.reflect.Type;
import java.util.Set;

import javax.ws.rs.Priorities;

import javax.annotation.Priority;

import org.glassfish.jersey.model.ContractProvider;

/**
 * Jersey ranked provider model.
 *
 * @param <T> service provider contract Java type.
 * @author Michal Gajdos
 */
public class RankedProvider<T> {

    private final T provider;
    private final int rank;
    private final Set<Type> contractTypes;

    /**
     * Creates a new {@code RankedProvider} instance. The rank of the provider is obtained from the {@link javax.annotation.Priority}
     * annotation or is set to {@value javax.ws.rs.Priorities#USER} if the annotation is not present.
     *
     * @param provider service provider to create a {@code RankedProvider} instance from.
     */
    public RankedProvider(final T provider) {
        this.provider = provider;
        this.rank = computeRank(provider, ContractProvider.NO_PRIORITY);
        this.contractTypes = null;
    }

    /**
     * Creates a new {@code RankedProvider} instance for given {@code provider} with specific {@code rank} (> 0).
     *
     * @param provider service provider to create a {@code RankedProvider} instance from.
     * @param rank rank of this provider.
     */
    public RankedProvider(final T provider, final int rank) {
        this(provider, rank, null);
    }

    /**
     * Creates a new {@code RankedProvider} instance for given {@code provider} with specific {@code rank} (> 0).
     *
     * @param provider service provider to create a {@code RankedProvider} instance from.
     * @param rank rank of this provider.
     * @param contracts contracts implemented by the service provider
     */
    public RankedProvider(final T provider, final int rank, final Set<Type> contracts) {
        this.provider = provider;
        this.rank = computeRank(provider, rank);
        this.contractTypes = contracts;
    }

    private int computeRank(final T provider, final int rank) {
        if (rank > 0) {
            return rank;
        } else {
            Class<?> clazz = provider.getClass();

            // when provided instance is a proxy (from weld), we need to get the right class to check for
            // @Priority annotation - proxy doesn't propagate isAnnotationPresent to the parent class.
            while (clazz.isSynthetic()) {
                clazz = clazz.getSuperclass();
            }

            if (clazz.isAnnotationPresent(Priority.class)) {
                return clazz.getAnnotation(Priority.class).value();
            } else {
                return Priorities.USER;
            }
        }
    }

    public T getProvider() {
        return provider;
    }

    public int getRank() {
        return rank;
    }

    /**
     * Get me set of implemented contracts.
     * Returns null if no contracts are implemented.
     *
     * @return set of contracts or null if no contracts have been implemented.
     */
    public Set<Type> getContractTypes() {
        return contractTypes;
    }

    @Override
    public String toString() {
        return provider.getClass().getName();
    }
}
