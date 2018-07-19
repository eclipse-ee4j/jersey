/*
 * Copyright (c) 2010, 2017 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;
import org.glassfish.jersey.internal.inject.InserterException;
import org.glassfish.jersey.client.inject.ParameterInserter;


/**
 * Insert value of the parameter using a single parameter value and the underlying
 * {@link ParamConverter param converter}.
 *
 * @param <T> custom Java type.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
final class SingleValueInserter<T> extends AbstractParamValueInserter<T> implements ParameterInserter<T, String> {

    /**
     * Create new single value inserter.
     *
     * @param converter          string value reader.
     * @param parameterName      string parameter name.
     * @param defaultValue       default value.
     */
    public SingleValueInserter(final ParamConverter<T> converter, final String parameterName, final String defaultValue) {
        super(converter, parameterName, defaultValue);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation inserts the value of the parameter applying the underlying
     * {@link ParamConverter param converter} to the first value found in the list of potential multiple
     * parameter values. Any other values in the multi-value list will be ignored.
     *
     * @param parameters map of parameters.
     * @return inserted single parameter value.
     */
    @Override
    public String insert(final T value){
        try {
            if (value == null && isDefaultValueRegistered()) {
                return getDefaultValueString();
            } else {
                return toString(value);
            }
        } catch (final WebApplicationException | ProcessingException ex) {
            throw ex;
        } catch (final IllegalArgumentException ex) {
            return defaultValue();
        } catch (final Exception ex) {
            throw new InserterException(ex);
        }
    }
}
