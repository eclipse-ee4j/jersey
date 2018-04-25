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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.ext.ParamConverter;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Implementation of {@link MultivaluedParameterExtractorProvider}. For each
 * parameter, the implementation obtains a {@link ParamConverter param converter} instance via
 * {@link ParamConverterFactory} and creates the proper
 * {@link MultivaluedParameterExtractor multivalued parameter extractor}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
final class MultivaluedParameterExtractorFactory implements MultivaluedParameterExtractorProvider {

    private final LazyValue<ParamConverterFactory> paramConverterFactory;

    /**
     * Create new multivalued map parameter extractor factory.
     *
     * @param paramConverterFactory string readers factory.
     */
    public MultivaluedParameterExtractorFactory(LazyValue<ParamConverterFactory> paramConverterFactory) {
        this.paramConverterFactory = paramConverterFactory;
    }

    @Override
    public MultivaluedParameterExtractor<?> get(final Parameter p) {
        return process(
                paramConverterFactory.get(),
                p.getDefaultValue(),
                p.getRawType(),
                p.getType(),
                p.getAnnotations(),
                p.getSourceName());
    }

    @SuppressWarnings("unchecked")
    private MultivaluedParameterExtractor<?> process(
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
                return new SingleValueExtractor(converter, parameterName, defaultValue);
            } catch (final ExtractorException e) {
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

            if (typePair == null || typePair.rawClass() == String.class) {
                return StringCollectionExtractor.getInstance(rawType, parameterName, defaultValue);
            } else {
                converter = paramConverterFactory.getConverter(typePair.rawClass(),
                        typePair.type(),
                        annotations);

                if (converter == null) {
                    return null;
                }

                try {
                    return CollectionExtractor.getInstance(rawType, converter, parameterName, defaultValue);
                } catch (final ExtractorException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new ProcessingException(LocalizationMessages.ERROR_PARAMETER_TYPE_PROCESSING(rawType), e);
                }
            }
        }

        // Check primitive types.
        if (rawType == String.class) {
            return new SingleStringValueExtractor(parameterName, defaultValue);
        } else if (rawType == Character.class) {
            return new PrimitiveCharacterExtractor(parameterName,
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
                return new PrimitiveCharacterExtractor(parameterName,
                        defaultValue,
                        PrimitiveMapper.primitiveToDefaultValueMap.get(wrappedRaw));
            }

            // Check for static valueOf(String)
            final Method valueOf = AccessController.doPrivileged(ReflectionHelper.getValueOfStringMethodPA(wrappedRaw));
            if (valueOf != null) {
                try {
                    return new PrimitiveValueOfExtractor(valueOf,
                            parameterName,
                            defaultValue,
                            PrimitiveMapper.primitiveToDefaultValueMap.get(wrappedRaw));
                } catch (final Exception e) {
                    throw new ProcessingException(LocalizationMessages.DEFAULT_COULD_NOT_PROCESS_METHOD(defaultValue, valueOf));
                }
            }

        }

        return null;
    }
}
