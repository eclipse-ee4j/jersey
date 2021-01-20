/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Array;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

/**
 * Extract parameter value as a typed array.
 *
 * @param <T> parameter value type.
 */
class ArrayExtractor<T> extends AbstractParamValueExtractor<T> implements MultivaluedParameterExtractor<T[]> {

    private final Class<?> type;

    /**
     * Create new array parameter extractor.
     *
     * @param type               the type class to manage runtime T generic.
     * @param converter          parameter converter to be used to convert parameter from a String.
     * @param parameterName      parameter name.
     * @param defaultStringValue default parameter String value.
     */
    protected ArrayExtractor(Class<?> type, ParamConverter<T> converter, String parameterName, String defaultStringValue) {
        super(converter, parameterName, defaultStringValue);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(getName());
        T[] args = null;
        if (stringList != null) {
            args = (T[]) Array.newInstance(type, stringList.size());
            for (int i = 0; i < stringList.size(); i++) {
                args[i] = fromString(stringList.get(i));
            }
        } else if (isDefaultValueRegistered()) {
            args = (T[]) Array.newInstance(type, 1);
            args[0] = defaultValue();
        } else {
            args = (T[]) new Object[0];
        }
        return args;
    }

    /**
     * Get string array extractor instance supporting.
     *
     * @param type           the type class to manage runtime generic.
     * @param parameterName  extracted parameter name.
     * @param defaultValueString   default parameter value.
     * @return string array extractor instance.
     */
    public static <T> ArrayExtractor<T> getInstance(Class<?> type,
            ParamConverter<T> converter,
            String parameterName,
            String defaultValueString) {
        return new ArrayExtractor<>(type, converter, parameterName, defaultValueString);
    }

}
