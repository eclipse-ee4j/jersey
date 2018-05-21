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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.internal.inject.InserterException;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Abstract base class for implementing parameter value inserter
 * logic supplied using {@link ParamConverter parameter converters}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
abstract class AbstractParamValueInserter<T> {

    private final ParamConverter<T> paramConverter;
    private final String parameterName;
    private final String defaultValue;
    private final UnsafeValue<String, RuntimeException> convertedDefaultValue;

    /**
     * Constructor that initializes parameter inserter.
     *
     * @param converter          parameter converter.
     * @param parameterName      name of the parameter.
     * @param defaultValueString default parameter value string.
     */
    protected AbstractParamValueInserter(ParamConverter<T> converter, String parameterName, final String defaultValue) {
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
     * Get the name of the parameter this inserter belongs to.
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
     * Insert parameter value to string using the configured {@link ParamConverter parameter converter}.
     *
     * A {@link WebApplicationException} / {@link IllegalArgumentException} thrown
     * from the converter is propagated unchanged. Any other exception throws by
     * the converter is wrapped in a new {@link InserterException} before rethrowing.
     *
     * @param value parameter value to be converted/inserted.
     * @return inserted value of a given Java type.
     * @throws WebApplicationException in case the underlying parameter converter throws
     * a {@code WebApplicationException}. The exception is rethrown without a change.
     * @throws InserterException wrapping any other exception thrown by the parameter converter.
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
            throw new InserterException(ex);
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
