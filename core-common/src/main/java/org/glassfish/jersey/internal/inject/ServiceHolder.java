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
import java.util.Set;

/**
 * Implementation of this class is used as a holder for service instance from
 * {@link InjectionManager} along with other information about the provided service.
 *
 * @see ServiceHolderImpl
 * @param <T>
 */
public interface ServiceHolder<T> {

    /**
     * An instance of the service got from {@link InjectionManager}.
     *
     * @return service instance.
     */
    T getInstance();

    /**
     * Gets an implementation class of the instance which is kept in this service holder.
     *
     * @return implementation class of the kept instance.
     */
    Class<T> getImplementationClass();

    /**
     * Gets all contracts which represents the kept instance.
     *
     * @return all contracts.
     */
    Set<Type> getContractTypes();

    /**
     * Gets ranking of the kept instance.
     *
     * @return instance's ranking.
     */
    int getRank();

}
