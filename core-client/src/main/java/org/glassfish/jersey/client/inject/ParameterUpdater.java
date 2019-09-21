/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.client.inject;

/**
 * Provider that converts the an object of a custom Java type
 * values to String / Collection&lt;String>&gt; type
 *
 * @param <T> custom Java type
 * @param <R> String / Collection&lt;String>&gt; type
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
public interface ParameterUpdater<T, R> {

    /**
     * Name of the parameter to be udpated
     *
     * @return name of the updated parameter.
     */
    String getName();

    /**
     * Default value (string) that will be used in case input value is not available.
     *
     * @return default (back-up) value.
     */
    String getDefaultValueString();

    /**
     * Update the value using ParamConverter#toString (and using
     * the configured {@link #getDefaultValueString() default value})
     *
     * @param parameters custom Java type instance value.
     * @return converted value.
     */
    R update(T parameters);
}
