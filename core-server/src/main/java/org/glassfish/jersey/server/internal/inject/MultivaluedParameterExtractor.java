/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Provider that converts the values of an entry of a given {@link #getName() name}
 * from the supplied {@link MultivaluedMap multivalued map} into an object of a custom
 * Java type.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface MultivaluedParameterExtractor<T> {

    /**
     * Name of the parameter (map key) to be extracted from the supplied
     * {@link MultivaluedMap multivalued map}.
     *
     * @return name of the extracted parameter.
     */
    String getName();

    /**
     * Default entry value (string) that will be used in case the entry
     * is not present in the supplied {@link MultivaluedMap multivalued map}.
     *
     * @return default (back-up) map entry value.
     */
    String getDefaultValueString();

    /**
     * Extract the map entry identified by a {@link #getName() name} (and using
     * the configured {@link #getDefaultValueString() default value}) from
     * the supplied {@link MultivaluedMap multivalued map}.
     *
     * @param parameters multivalued parameter map.
     * @return custom Java type instance representing the extracted multivalued
     *         map entry.
     */
    T extract(MultivaluedMap<String, String> parameters);
}
