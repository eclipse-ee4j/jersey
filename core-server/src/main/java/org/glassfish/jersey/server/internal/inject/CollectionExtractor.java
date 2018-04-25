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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Extract parameter value as a typed collection.
 *
 * @param <T> parameter value type.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class CollectionExtractor<T> extends AbstractParamValueExtractor<T>
        implements MultivaluedParameterExtractor<Collection<T>> {

    /**
     * Create new collection parameter extractor.
     *
     * @param converter          parameter converter to be used to convert parameter from a String.
     * @param parameterName      parameter name.
     * @param defaultStringValue default parameter String value.
     */
    protected CollectionExtractor(final ParamConverter<T> converter,
                                  final String parameterName,
                                  final String defaultStringValue) {
        super(converter, parameterName, defaultStringValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<T> extract(final MultivaluedMap<String, String> parameters) {
        final List<String> stringList = parameters.get(getName());

        final Collection<T> valueList = newCollection();
        if (stringList != null) {
            for (final String v : stringList) {
                valueList.add(fromString(v));
            }
        } else if (isDefaultValueRegistered()) {
            valueList.add(defaultValue());
        }

        return valueList;
    }

    /**
     * Get a new collection instance that will be used to store the extracted parameters.
     * <p/>
     * The method is overridden by concrete implementations to return an instance
     * of a proper collection sub-type.
     *
     * @return instance of a proper collection sub-type
     */
    protected abstract Collection<T> newCollection();

    private static final class ListValueOf<T> extends CollectionExtractor<T> {

        ListValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected List<T> newCollection() {
            return new ArrayList<>();
        }
    }

    private static final class SetValueOf<T> extends CollectionExtractor<T> {

        SetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected Set<T> newCollection() {
            return new HashSet<>();
        }
    }

    private static final class SortedSetValueOf<T> extends CollectionExtractor<T> {

        SortedSetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValueString) {
            super(converter, parameter, defaultValueString);
        }

        @Override
        protected SortedSet<T> newCollection() {
            return new TreeSet<>();
        }
    }

    /**
     * Get a new {@code CollectionExtractor} instance.
     *
     * @param collectionType     raw collection type.
     * @param converter          parameter converter to be used to convert parameter string values into
     *                           values of the requested Java type.
     * @param parameterName      parameter name.
     * @param defaultValueString default parameter string value.
     * @param <T>                converted parameter Java type.
     * @return new collection parameter extractor instance.
     */
    public static <T> CollectionExtractor getInstance(final Class<?> collectionType,
                                                      final ParamConverter<T> converter,
                                                      final String parameterName,
                                                      final String defaultValueString) {
        if (List.class == collectionType) {
            return new ListValueOf<>(converter, parameterName, defaultValueString);
        } else if (Set.class == collectionType) {
            return new SetValueOf<>(converter, parameterName, defaultValueString);
        } else if (SortedSet.class == collectionType) {
            return new SortedSetValueOf<>(converter, parameterName, defaultValueString);
        } else {
            throw new ProcessingException(LocalizationMessages.COLLECTION_EXTRACTOR_TYPE_UNSUPPORTED());
        }
    }
}
