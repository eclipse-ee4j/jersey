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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ParamConverter;

import org.glassfish.jersey.internal.inject.ExtractorException;

/**
 * Extract value of the parameter using a single parameter value and the underlying
 * {@link ParamConverter param converter}.
 *
 * @param <T> extracted Java type.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class SingleValueExtractor<T> extends AbstractParamValueExtractor<T> implements MultivaluedParameterExtractor<T> {

    /**
     * Create new single value extractor.
     *
     * @param converter          string value reader.
     * @param parameterName      string parameter name.
     * @param defaultStringValue default string value.
     */
    public SingleValueExtractor(final ParamConverter<T> converter, final String parameterName, final String defaultStringValue) {
        super(converter, parameterName, defaultStringValue);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation extracts the value of the parameter applying the underlying
     * {@link ParamConverter param converter} to the first value found in the list of potential multiple
     * parameter values. Any other values in the multi-value list will be ignored.
     *
     * @param parameters map of parameters.
     * @return extracted single parameter value.
     */
    @Override
    public T extract(final MultivaluedMap<String, String> parameters) {
        final String value = parameters.getFirst(getName());
        try {
            return fromString((value == null && isDefaultValueRegistered()) ? getDefaultValueString() : value);
        } catch (final WebApplicationException | ProcessingException ex) {
            throw ex;
        } catch (final IllegalArgumentException ex) {
            return defaultValue();
        } catch (final Exception ex) {
            throw new ExtractorException(ex);
        }
    }
}
