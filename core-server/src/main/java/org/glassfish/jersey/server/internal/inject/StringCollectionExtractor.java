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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Extract parameter value as a specific {@code String} Java collection type.
 * <p />
 * This class can be seen as a special, optimized, case of {@link CollectionExtractor}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class StringCollectionExtractor implements MultivaluedParameterExtractor<Collection<String>> {

    private final String parameter;
    private final String defaultValue;

    /**
     * Create new string collection parameter extractor.
     *
     * @param parameterName parameter name.
     * @param defaultValue  default parameter value.
     */
    protected StringCollectionExtractor(String parameterName, String defaultValue) {
        this.parameter = parameterName;
        this.defaultValue = defaultValue;
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
    public Collection<String> extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(parameter);

        final Collection<String> collection = newCollection();
        if (stringList != null) {
            collection.addAll(stringList);
        } else if (defaultValue != null) {
            collection.add(defaultValue);
        }

        return collection;
    }

    /**
     * Get a new string collection instance that will be used to store the extracted parameters.
     *
     * The method is overridden by concrete implementations to return an instance
     * of a proper collection sub-type.
     *
     * @return instance of a proper collection sub-type
     */
    protected abstract Collection<String> newCollection();

    private static final class ListString extends StringCollectionExtractor {

        public ListString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected List<String> newCollection() {
            return new ArrayList<String>();
        }
    }

    private static final class SetString extends StringCollectionExtractor {

        public SetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected Set<String> newCollection() {
            return new HashSet<String>();
        }
    }

    private static final class SortedSetString extends StringCollectionExtractor {

        public SortedSetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected SortedSet<String> newCollection() {
            return new TreeSet<String>();
        }
    }

    /**
     * Get string collection extractor instance supporting the given collection
     * class type for the parameter specified.
     *
     * @param collectionType collection type to be supported by the extractor.
     * @param parameterName  extracted parameter name.
     * @param defaultValue   default parameter value.
     * @return string collection extractor instance supporting the given collection
     *         class type.
     */
    public static StringCollectionExtractor getInstance(Class<?> collectionType, String parameterName, String defaultValue) {
        if (List.class == collectionType) {
            return new ListString(parameterName, defaultValue);
        } else if (Set.class == collectionType) {
            return new SetString(parameterName, defaultValue);
        } else if (SortedSet.class == collectionType) {
            return new SortedSetString(parameterName, defaultValue);
        } else {
            throw new RuntimeException("Unsupported collection type: " + collectionType.getName());
        }
    }
}
