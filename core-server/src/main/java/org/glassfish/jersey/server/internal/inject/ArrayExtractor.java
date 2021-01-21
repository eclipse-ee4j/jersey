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
import java.util.function.Function;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

/**
 * Extract parameter value as an array.
 *
 */
class ArrayExtractor implements MultivaluedParameterExtractor<Object> {

    private final String parameterName;
    private final String defaultValueString;
    private final Function<String, Object> typeExtractor;
    private final Class<?> type;

    private ArrayExtractor(Class<?> type, Function<String, Object> typeExtractor,
            String parameterName, String defaultValueString) {
        this.type = type;
        this.typeExtractor = typeExtractor;
        this.parameterName = parameterName;
        this.defaultValueString = defaultValueString;
    }

    @Override
    public String getName() {
        return parameterName;
    }

    @Override
    public String getDefaultValueString() {
        return defaultValueString;
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(getName());
        Object array = null;
        if (stringList != null) {
            array = Array.newInstance(type, stringList.size());
            for (int i = 0; i < stringList.size(); i++) {
                Array.set(array, i, typeExtractor.apply(stringList.get(i)));
            }
        } else if (defaultValueString != null) {
            array = Array.newInstance(type, 1);
            Array.set(array, 0, typeExtractor.apply(defaultValueString));
        } else {
            array = Array.newInstance(type, 0);
        }
        return array;
    }

    /**
     * Get array extractor instance supporting.
     *
     * @param type           the type class to manage runtime generic.
     * @param converter  the converter of the type.
     * @param parameterName  extracted parameter name.
     * @param defaultValueString   default parameter value.
     * @return string array extractor instance.
     */
    public static MultivaluedParameterExtractor<Object> getInstance(Class<?> type,
            ParamConverter<?> converter, String parameterName, String defaultValueString) {
        Function<String, Object> typeExtractor = value -> converter.fromString(value);
        return new ArrayExtractor(type, typeExtractor, parameterName, defaultValueString);
    }

    /**
     * Get array extractor instance supporting.
     *
     * @param type           the type class to manage runtime generic.
     * @param extractor  the extractor.
     * @param parameterName  extracted parameter name.
     * @param defaultValueString   default parameter value.
     * @return string array extractor instance.
     */
    public static MultivaluedParameterExtractor<Object> getInstance(Class<?> type,
            MultivaluedParameterExtractor<?> extractor,
            String parameterName,
            String defaultValueString) {
        Function<String, Object> typeExtractor = value -> {
            MultivaluedMap<String, String> pair = new MultivaluedHashMap<>();
            pair.putSingle(parameterName, value);
            return extractor.extract(pair);
        };
        return new ArrayExtractor(type, typeExtractor, parameterName, defaultValueString);
    }
}
