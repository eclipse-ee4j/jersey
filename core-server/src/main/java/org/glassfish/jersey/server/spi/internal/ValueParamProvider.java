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

package org.glassfish.jersey.server.spi.internal;

import java.util.function.Function;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.spi.Contract;

/**
 * Parameter value factory SPI.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Michal Gajdos
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ValueParamProvider {

    /**
     * Get an injected value provider for the parameter. May return {@code null}
     * in case the parameter is not supported by the value provider.
     *
     * @param parameter parameter requesting the value provider instance.
     * @return injected parameter value provider. Returns {@code null} if parameter is not supported.
     */
    Function<ContainerRequest, ?> getValueProvider(Parameter parameter);

    /**
     * Gets the priority of this provider.
     *
     * @return the priority of this provider.
     * @see PriorityType
     * @see Priority
     */
    PriorityType getPriority();

    /**
     * Priorities are intended to be used as a means to determine the order in which objects are considered whether they are
     * suitable for a particular action or not (e.g. providing a service like creating a value supplier for an injectable
     * parameter).
     * The higher the weight of a priority is the sooner should be an object with this priority examined.
     * <p/>
     * If two objects are of the same priority there is no guarantee which one comes first.
     *
     * @see ValueParamProvider.Priority
     */
    interface PriorityType {

        /**
         * Returns the weight of this priority.
         *
         * @return weight of this priority.
         */
        public int getWeight();

    }

    /**
     * Enumeration of priorities for providers (e.g. {@code ValueSupplierProvider}). At first providers with the {@code HIGH}
     * priority are examined then those with {@code NORMAL} priority and at last the ones with the {@code LOW} priority.
     */
    enum Priority implements PriorityType {
        /**
         * Low priority.
         */
        LOW(100),
        /**
         * Normal priority.
         */
        NORMAL(200),
        /**
         * High priority.
         */
        HIGH(300);

        /**
         * Weight of this priority.
         */
        private final int weight;

        private Priority(int weight) {
            this.weight = weight;
        }

        @Override
        public int getWeight() {
            return weight;
        }

    }
}
