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

package org.glassfish.jersey.client.internal.inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import static java.util.stream.Collectors.toList;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.client.inject.ParameterUpdater;

/**
 * Update parameter value as a typed collection.
 *
 * @param <T> parameter value type.
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
abstract class CollectionUpdater<T> extends AbstractParamValueUpdater<T>
        implements ParameterUpdater<Collection<T>, Collection<String>> {

    /**
     * Create new collection parameter updater.
     *
     * @param converter          parameter converter to be used to convert parameter from a custom Java type.
     * @param parameterName      parameter name.
     * @param defaultValue default parameter String value.
     */
    protected CollectionUpdater(final ParamConverter<T> converter,
                                final String parameterName,
                                final String defaultValue) {
        super(converter, parameterName, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> update(final Collection<T> values) {
        Collection<String> results = Collections.EMPTY_LIST;
        if (values != null) {
            results = values
                    .stream()
                    .map(item -> toString(item))
                    .collect(toList());
        } else if (isDefaultValueRegistered()) {
            results = Collections.singletonList(getDefaultValueString());
        }
        return results;
    }

    /**
     * Get a new collection instance that will be used to store the updated parameters.
     * <p/>
     * The method is overridden by concrete implementations to return an instance
     * of a proper collection sub-type.
     *
     * @return instance of a proper collection sub-type
     */
    protected abstract Collection<String> newCollection();

    private static final class ListValueOf<T> extends CollectionUpdater<T> {

        ListValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected List<String> newCollection() {
            return new ArrayList<>();
        }
    }

    private static final class SetValueOf<T> extends CollectionUpdater<T> {

        SetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected Set<String> newCollection() {
            return new HashSet<>();
        }
    }

    private static final class SortedSetValueOf<T> extends CollectionUpdater<T> {

        SortedSetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected SortedSet<String> newCollection() {
            return new TreeSet<>();
        }
    }

    /**
     * Get a new {@code CollectionUpdater} instance.
     *
     * @param collectionType     raw collection type.
     * @param converter          parameter converter to be used to convert parameter Java type values into
     *                           String values .
     * @param parameterName      parameter name.
     * @param defaultValue default parameter string value.
     * @param <T>                converted parameter Java type.
     * @return new collection parameter updated instance.
     */
    public static <T> CollectionUpdater getInstance(final Class<?> collectionType,
                                                    final ParamConverter<T> converter,
                                                    final String parameterName,
                                                    final String defaultValue) {
        if (List.class == collectionType) {
            return new ListValueOf<>(converter, parameterName, defaultValue);
        } else if (Set.class == collectionType) {
            return new SetValueOf<>(converter, parameterName, defaultValue);
        } else if (SortedSet.class == collectionType) {
            return new SortedSetValueOf<>(converter, parameterName, defaultValue);
        } else {
            throw new ProcessingException(LocalizationMessages.COLLECTION_UPDATER_TYPE_UNSUPPORTED());
        }
    }
}
