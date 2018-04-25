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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Abstract base class for implementing multivalued parameter value extractor
 * logic supplied using {@link ParamConverter parameter converters}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class AbstractParamValueExtractor<T> {

    private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValueString;
    private final UnsafeValue<T, RuntimeException> convertedDefaultValue;

    /**
     * Constructor that initializes common string reader-based parameter extractor
     * data.
     * <p />
     * As part of the initialization, the default value validation is performed
     * based on the presence and value of the {@link ParamConverter.Lazy}
     * annotation on the supplied string value reader class.
     *
     * @param converter          parameter converter.
     * @param parameterName      name of the parameter.
     * @param defaultValueString default parameter value string.
     */
    protected AbstractParamValueExtractor(ParamConverter<T> converter, String parameterName, final String defaultValueString) {
        this.paramConverter = converter;
        this.parameterName = parameterName;
        this.defaultValueString = defaultValueString;


        if (defaultValueString != null) {
            this.convertedDefaultValue = Values.lazy(new UnsafeValue<T, RuntimeException>() {
                @Override
                public T get() throws RuntimeException {
                    return convert(defaultValueString);
                }
            });

            if (!converter.getClass().isAnnotationPresent(ParamConverter.Lazy.class)) {
                // ignore return value - executed just for validation reasons
                convertedDefaultValue.get();
            }
        } else {
            convertedDefaultValue = null;
        }
    }

    /**
     * Get the name of the parameter this extractor belongs to.
     *
     * @return parameter name.
     */
    public String getName() {
        return parameterName;
    }

    /**
     * Get the default string value of the parameter.
     *
     * @return default parameter string value.
     */
    public String getDefaultValueString() {
        return defaultValueString;
    }

    /**
     * Extract parameter value from string using the configured {@link ParamConverter parameter converter}.
     *
     * A {@link WebApplicationException} thrown from the converter is propagated
     * unchanged. Any other exception throws by the converter is wrapped in a new
     * {@link ExtractorException} before rethrowing.
     *
     * @param value parameter string value to be converted/extracted.
     * @return extracted value of a given Java type.
     * @throws WebApplicationException in case the underlying parameter converter throws a {@code WebApplicationException}.
     *                                 The exception is rethrown without a change.
     * @throws ExtractorException      wrapping any other exception thrown by the parameter converter.
     */
    protected final T fromString(String value) {
        T result = convert(value);
        if (result == null) {
            return defaultValue();
        }
        return result;
    }

    private T convert(String value) {
        try {
            return paramConverter.fromString(value);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception ex) {
            throw new ExtractorException(ex);
        }
    }

    /**
     * Check if there is a default string value registered for the parameter.
     *
     * @return {@code true} if there is a default parameter string value registered, {@code false} otherwise.
     */
    protected final boolean isDefaultValueRegistered() {
        return defaultValueString != null;
    }

    /**
     * Get converted default value.
     *
     * The conversion happens lazily during first call of the method.
     *
     * @return converted default value.
     */
    protected final T defaultValue() {
        if (!isDefaultValueRegistered()) {
            return null;
        }

        return convertedDefaultValue.get();
    }
}
