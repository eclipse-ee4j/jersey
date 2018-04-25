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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.internal.inject.ExtractorException;

/**
 * Extract primitive parameter value from the {@link MultivaluedMap multivalued parameter map}
 * using one of the {@code valueOf(String)} methods on the primitive Java type wrapper
 * classes.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PrimitiveValueOfExtractor implements MultivaluedParameterExtractor<Object> {

    private final Method valueOf;
    private final String parameter;
    private final String defaultStringValue;
    private final Object defaultValue;
    private final Object defaultPrimitiveTypeValue;

    /**
     * Create new primitive parameter value extractor.
     *
     * @param valueOf                   {@code valueOf()} method handler.
     * @param parameter                 string parameter value.
     * @param defaultStringValue        default string value.
     * @param defaultPrimitiveTypeValue default primitive type value.
     */
    public PrimitiveValueOfExtractor(Method valueOf, String parameter,
                                     String defaultStringValue, Object defaultPrimitiveTypeValue) {
        this.valueOf = valueOf;
        this.parameter = parameter;
        this.defaultStringValue = defaultStringValue;
        this.defaultValue = (defaultStringValue != null)
                ? getValue(defaultStringValue) : null;
        this.defaultPrimitiveTypeValue = defaultPrimitiveTypeValue;
    }

    @Override
    public String getName() {
        return parameter;
    }

    @Override
    public String getDefaultValueString() {
        return defaultStringValue;
    }

    private Object getValue(String v) {
        try {
            return valueOf.invoke(null, v);
        } catch (InvocationTargetException ex) {
            Throwable target = ex.getTargetException();
            if (target instanceof WebApplicationException) {
                throw (WebApplicationException) target;
            } else {
                throw new ExtractorException(target);
            }
        } catch (Exception ex) {
            throw new ProcessingException(ex);
        }
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(parameter);
        if (v != null && !v.trim().isEmpty()) {
            return getValue(v);
        } else if (defaultValue != null) {
            // TODO do we need to clone the default value?
            return defaultValue;
        }

        return defaultPrimitiveTypeValue;
    }
}
