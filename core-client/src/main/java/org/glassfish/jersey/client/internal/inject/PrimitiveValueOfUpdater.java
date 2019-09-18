/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, 2019 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.client.internal.inject;

import org.glassfish.jersey.client.inject.ParameterUpdater;

/**
 * Update String value using {@code toString()} methods
 * on the primitive Java type wrapper classes.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 * @author Patrik Dudits
 *
 */
final class PrimitiveValueOfUpdater implements ParameterUpdater<Object, String> {

    private final String parameter;
    private final String defaultValue;
    private final Object defaultPrimitiveTypeValue;

    /**
     * Create new primitive parameter value updater.
     *
     * @param valueOf {@code valueOf()} method handler.
     * @param parameter string parameter value.
     * @param defaultValue default string value.
     * @param defaultPrimitiveTypeValue default primitive type value.
     */
    public PrimitiveValueOfUpdater(String parameter,
                                   String defaultValue,
                                   Object defaultPrimitiveTypeValue) {
        this.parameter = parameter;
        this.defaultValue = defaultValue;
        this.defaultPrimitiveTypeValue = defaultPrimitiveTypeValue;
    }

    @Override
    public String getName() {
        return parameter;
    }

    @Override
    public String getDefaultValueString() {
        return defaultValue;
    }

    @Override
    public String update(Object value) {
        if (value != null) {
            return value.toString();
        } else if (defaultValue != null) {
            return defaultValue;
        }
        return defaultPrimitiveTypeValue.toString();
    }
}
