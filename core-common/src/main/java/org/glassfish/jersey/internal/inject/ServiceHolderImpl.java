/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of the instance keeper which kept the instance of the class from
 * {@link InjectionManager} and the other information about this instance.
 *
 * @param <T> type of the kept instance.
 */
public class ServiceHolderImpl<T> implements ServiceHolder<T> {

    private final T service;

    private final Class<T> implementationClass;

    private final Set<Type> contractTypes;

    private final int rank;

    /**
     * Creates a new instance of the service holder which keeps the concrete instance and its additional information.
     *
     * @param service             service instance kept by this holder.
     * @param contractTypes       types which represent the given instance.
     */
    @SuppressWarnings("unchecked")
    public ServiceHolderImpl(T service, Set<Type> contractTypes) {
        this(service, (Class<T>) service.getClass(), contractTypes, 0);
    }

    /**
     * Creates a new instance of the service holder which keeps the concrete instance and its additional information.
     *
     * @param service             service instance kept by this holder.
     * @param implementationClass implementation class of the given instance.
     * @param contractTypes       types which represent the given instance.
     * @param rank                ranking of the given instance.
     */
    public ServiceHolderImpl(T service, Class<T> implementationClass, Set<Type> contractTypes, int rank) {
        this.service = service;
        this.implementationClass = implementationClass;
        this.contractTypes = contractTypes;
        this.rank = rank;
    }

    @Override
    public T getInstance() {
        return service;
    }

    @Override
    public Class<T> getImplementationClass() {
        return implementationClass;
    }

    @Override
    public Set<Type> getContractTypes() {
        return contractTypes;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceHolderImpl)) {
            return false;
        }
        ServiceHolderImpl<?> that = (ServiceHolderImpl<?>) o;
        return rank == that.rank
                && Objects.equals(service, that.service)
                && Objects.equals(implementationClass, that.implementationClass)
                && Objects.equals(contractTypes, that.contractTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, implementationClass, contractTypes, rank);
    }
}
