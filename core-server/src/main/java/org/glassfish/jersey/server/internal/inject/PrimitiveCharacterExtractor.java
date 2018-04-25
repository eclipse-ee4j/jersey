/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Value extractor for {@link java.lang.Character} and {@code char} parameters.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class PrimitiveCharacterExtractor implements MultivaluedParameterExtractor<Object> {

    final String parameter;
    final String defaultStringValue;
    final Object defaultPrimitiveTypeValue;

    public PrimitiveCharacterExtractor(String parameter, String defaultStringValue, Object defaultPrimitiveTypeValue) {
        this.parameter = parameter;
        this.defaultStringValue = defaultStringValue;
        this.defaultPrimitiveTypeValue = defaultPrimitiveTypeValue;
    }

    @Override
    public String getName() {
        return parameter;
    }

    @Override
    public String getDefaultValueString() {
        return defaultStringValue;
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(parameter);
        if (v != null && !v.trim().isEmpty()) {
            if (v.length() == 1) {
                return v.charAt(0);
            } else {
                throw new ExtractorException(LocalizationMessages.ERROR_PARAMETER_INVALID_CHAR_VALUE(v));
            }
        } else if (defaultStringValue != null && !defaultStringValue.trim().isEmpty()) {
            if (defaultStringValue.length() == 1) {
                return defaultStringValue.charAt(0);
            } else {
                throw new ExtractorException(LocalizationMessages.ERROR_PARAMETER_INVALID_CHAR_VALUE(defaultStringValue));
            }
        }

        return defaultPrimitiveTypeValue;
    }
}
