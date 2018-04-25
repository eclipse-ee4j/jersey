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

import javax.ws.rs.core.MultivaluedMap;

/**
 * Extract value of the parameter by returning the first string parameter value
 * found in the list of string parameter values.
 * <p />
 * This class can be seen as a special, optimized, case of {@link SingleValueExtractor}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class SingleStringValueExtractor implements MultivaluedParameterExtractor<String> {

    private final String paramName;
    private final String defaultValue;

    /**
     * Create new single string value extractor.
     *
     * @param parameterName string parameter name.
     * @param defaultValue  default value.
     */
    public SingleStringValueExtractor(String parameterName, String defaultValue) {
        this.paramName = parameterName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return paramName;
    }

    @Override
    public String getDefaultValueString() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation return s the first String value found in the list of
     * potential multiple string parameter values. Any other values in the multi-value
     * list will be ignored.
     *
     * @param parameters map of parameters.
     * @return extracted single string parameter value.
     */
    @Override
    public String extract(MultivaluedMap<String, String> parameters) {
        String value = parameters.getFirst(paramName);
        return (value != null) ? value : defaultValue;
    }
}
