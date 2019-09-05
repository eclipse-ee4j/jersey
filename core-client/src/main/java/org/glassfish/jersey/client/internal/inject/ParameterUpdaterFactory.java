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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;
import javax.inject.Singleton;
import org.glassfish.jersey.internal.inject.UpdaterException;
import org.glassfish.jersey.internal.inject.ParamConverterFactory;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.model.Parameter;
import org.glassfish.jersey.internal.inject.PrimitiveMapper;
import org.glassfish.jersey.client.inject.ParameterUpdater;
import org.glassfish.jersey.client.inject.ParameterUpdaterProvider;

/**
 * Implementation of {@link ParameterUpdaterProvider}. For each
 * parameter, the implementation obtains a
 * {@link ParamConverter param converter} instance via
 * {@link ParamConverterFactory} and creates the proper
 * {@link ParameterUpdater parameter updater}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
@Singleton
final class ParameterUpdaterFactory implements ParameterUpdaterProvider {

    private final LazyValue<ParamConverterFactory> paramConverterFactory;

    /**
     * Create new parameter updater factory.
     *
     * @param paramConverterFactory string readers factory.
     */
    public ParameterUpdaterFactory(LazyValue<ParamConverterFactory> paramConverterFactory) {
        this.paramConverterFactory = paramConverterFactory;
    }

    @Override
    public ParameterUpdater<?, ?> get(final Parameter p) {
        return process(
                paramConverterFactory.get(),
                p.getDefaultValue(),
                p.getRawType(),
                p.getType(),
                p.getAnnotations(),
                p.getSourceName());
    }

    @SuppressWarnings("unchecked")
    private ParameterUpdater<?, ?> process(
            final ParamConverterFactory paramConverterFactory,
            final String defaultValue,
            final Class<?> rawType,
            final Type type,
            final Annotation[] annotations,
            final String parameterName) {

        // Try to find a converter that support rawType and type at first.
        // E.g. if someone writes a converter that support List<Integer> this approach should precede the next one.
        ParamConverter<?> converter = paramConverterFactory.getConverter(rawType, type, annotations);
        if (converter != null) {
            try {
                return new SingleValueUpdater(converter, parameterName, defaultValue);
            } catch (final UpdaterException e) {
                throw e;
            } catch (final Exception e) {
                throw new ProcessingException(LocalizationMessages.ERROR_PARAMETER_TYPE_PROCESSING(rawType), e);
            }
        }

        // Check whether the rawType is the type of the collection supported.
        if (rawType == List.class || rawType == Set.class || rawType == SortedSet.class) {
            // Get the generic type of the list. If none is found default to String.
            final List<ClassTypePair> typePairs = ReflectionHelper.getTypeArgumentAndClass(type);
            final ClassTypePair typePair = (typePairs.size() == 1) ? typePairs.get(0) : null;

            if (typePair != null) {
                converter = paramConverterFactory.getConverter(
                        typePair.rawClass(),
                        typePair.type(),
                        annotations
                );
            }
            if (converter != null) {
                try {
                    return CollectionUpdater.getInstance(rawType, converter, parameterName, defaultValue);
                } catch (final UpdaterException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new ProcessingException(LocalizationMessages.ERROR_PARAMETER_TYPE_PROCESSING(rawType), e);
                }
            }
        }

        // Check primitive types.
        if (rawType == String.class) {
            return new SingleStringValueUpdater(parameterName, defaultValue);
        } else if (rawType == Character.class) {
            return new PrimitiveCharacterUpdater(parameterName,
                    defaultValue,
                    PrimitiveMapper.primitiveToDefaultValueMap.get(rawType));
        } else if (rawType.isPrimitive()) {
            // Convert primitive to wrapper class
            final Class<?> wrappedRaw = PrimitiveMapper.primitiveToClassMap.get(rawType);
            if (wrappedRaw == null) {
                // Primitive type not supported
                return null;
            }

            if (wrappedRaw == Character.class) {
                return new PrimitiveCharacterUpdater(parameterName,
                        defaultValue,
                        PrimitiveMapper.primitiveToDefaultValueMap.get(wrappedRaw));
            }

            return new PrimitiveValueOfUpdater(
                    parameterName,
                    defaultValue,
                    PrimitiveMapper.primitiveToDefaultValueMap.get(wrappedRaw));
        }

        return null;
    }
}
