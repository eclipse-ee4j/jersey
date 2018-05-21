/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

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
import org.glassfish.jersey.client.inject.ParameterInserter;

/**
 * Insert parameter value as a typed collection.
 *
 * @param <T> parameter value type.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
abstract class CollectionInserter<T> extends AbstractParamValueInserter<T>
        implements ParameterInserter<Collection<T>, Collection<String>> {

    /**
     * Create new collection parameter inserter.
     *
     * @param converter          parameter converter to be used to convert parameter from a custom Java type.
     * @param parameterName      parameter name.
     * @param defaultValue default parameter String value.
     */
    protected CollectionInserter(final ParamConverter<T> converter,
                                  final String parameterName,
                                  final String defaultValue) {
        super(converter, parameterName, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> insert(final Collection<T> values) {
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
     * Get a new collection instance that will be used to store the inserted parameters.
     * <p/>
     * The method is overridden by concrete implementations to return an instance
     * of a proper collection sub-type.
     *
     * @return instance of a proper collection sub-type
     */
    protected abstract Collection<String> newCollection();

    private static final class ListValueOf<T> extends CollectionInserter<T> {

        ListValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected List<String> newCollection() {
            return new ArrayList<>();
        }
    }

    private static final class SetValueOf<T> extends CollectionInserter<T> {

        SetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected Set<String> newCollection() {
            return new HashSet<>();
        }
    }

    private static final class SortedSetValueOf<T> extends CollectionInserter<T> {

        SortedSetValueOf(final ParamConverter<T> converter, final String parameter, final String defaultValue) {
            super(converter, parameter, defaultValue);
        }

        @Override
        protected SortedSet<String> newCollection() {
            return new TreeSet<>();
        }
    }

    /**
     * Get a new {@code CollectionInserter} instance.
     *
     * @param collectionType     raw collection type.
     * @param converter          parameter converter to be used to convert parameter Java type values into
     *                           String values .
     * @param parameterName      parameter name.
     * @param defaultValue default parameter string value.
     * @param <T>                converted parameter Java type.
     * @return new collection parameter inserter instance.
     */
    public static <T> CollectionInserter getInstance(final Class<?> collectionType,
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
            throw new ProcessingException(LocalizationMessages.COLLECTION_INSERTER_TYPE_UNSUPPORTED());
        }
    }
}
