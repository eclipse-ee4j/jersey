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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.internal.inject.UpdaterException;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Abstract base class for implementing parameter value updater
 * logic supplied using {@link ParamConverter parameter converters}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
abstract class AbstractParamValueUpdater<T> {

    private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValue;
    private final UnsafeValue<String, RuntimeException> convertedDefaultValue;

    /**
     * Constructor that initializes parameter updater.
     *
     * @param converter          parameter converter.
     * @param parameterName      name of the parameter.
     * @param defaultValueString default parameter value string.
     */
    protected AbstractParamValueUpdater(ParamConverter<T> converter, String parameterName, final String defaultValue) {
        this.paramConverter = converter;
        this.parameterName = parameterName;
        this.defaultValue = defaultValue;

        if (defaultValue != null) {
            this.convertedDefaultValue = Values.lazy(new UnsafeValue<String, RuntimeException>() {
                @Override
                public String get() throws RuntimeException {
                    return defaultValue;
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
     * Get the name of the parameter this updater belongs to.
     *
     * @return parameter name.
     */
    public String getName() {
        return parameterName;
    }

    /**
     * Get the default value of the parameter.
     *
     * @return default parameter value.
     */
    public String getDefaultValueString() {
        return defaultValue;
    }

    /**
     * Update parameter value to string using the configured {@link ParamConverter parameter converter}.
     *
     * A {@link WebApplicationException} / {@link IllegalArgumentException} thrown
     * from the converter is propagated unchanged. Any other exception throws by
     * the converter is wrapped in a new {@link UpdaterException} before rethrowing.
     *
     * @param value parameter value to be converted/updated.
     * @return updated value of a given Java type.
     * @throws WebApplicationException in case the underlying parameter converter throws
     * a {@code WebApplicationException}. The exception is rethrown without a change.
     * @throws UpdaterException wrapping any other exception thrown by the parameter converter.
     */
    protected final String toString(T value) {
        String result = convert(value);
        if (result == null) {
            return defaultValue();
        }
        return result;
    }

    private String convert(T value) {
        try {
            return paramConverter.toString(value);
        } catch (WebApplicationException | IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UpdaterException(ex);
        }
    }

    /**
     * Check if there is a default value registered for the parameter.
     *
     * @return {@code true} if there is a default parameter value registered, {@code false} otherwise.
     */
    protected final boolean isDefaultValueRegistered() {
        return defaultValue != null;
    }

    /**
     * Get converted default value.
     *
     * The conversion happens lazily during first call of the method.
     *
     * @return converted default value.
     */
    protected final String defaultValue() {
        if (!isDefaultValueRegistered()) {
            return null;
        }

        return convertedDefaultValue.get();
    }
}
