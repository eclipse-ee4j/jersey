/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.internal;

import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.core.Configuration;

/**
 *  Resolver of a property value for the specified property {@code name} from the
 *  request-specific property bag, or the {@link Configuration global runtime configuration}.
 */
public interface PropertiesResolver {
    /**
     * Resolve a property value for the specified property {@code name}.
     *
     * <p>
     * The method returns the value of the property registered in the request-specific
     * property bag, if available. If no property for the given property name is found
     * in the request-specific property bag, the method looks at the properties stored
     * in the {@link Configuration global runtime configuration} this request
     * belongs to. If there is a value defined in the runtime configuration,
     * it is returned, otherwise the method returns {@code null} if no such property is
     * registered neither in the runtime nor in the request-specific property bag.
     * </p>
     *
     * @param name property name.
     * @param type expected property class type.
     * @param <T> property Java type.
     * @return resolved property value or {@code null} if no such property is registered.
     */
    public <T> T resolveProperty(final String name, final Class<T> type);

    /**
     * Resolve a property value for the specified property {@code name}.
     *
     * <p>
     * The method returns the value of the property registered in the request-specific
     * property bag, if available. If no property for the given property name is found
     * in the request-specific property bag, the method looks at the properties stored
     * in the {@link Configuration global runtime configuration} this request
     * belongs to. If there is a value defined in the runtime configuration,
     * it is returned, otherwise the method returns {@code defaultValue} if no such property is
     * registered neither in the runtime nor in the request-specific property bag.
     * </p>
     *
     * @param name property name.
     * @param defaultValue default value to return if the property is not registered.
     * @param <T> property Java type.
     * @return resolved property value or {@code defaultValue} if no such property is registered.
     */
    public <T> T resolveProperty(final String name, final T defaultValue);

    /**
     * Return new instance of {@link PropertiesResolver}.
     * @param configuration Runtime {@link Configuration}.
     * @param delegate Request scoped {@link PropertiesDelegate properties delegate}.
     * @return A new instance of {@link PropertiesResolver}.
     */
    public static PropertiesResolver create(Configuration configuration, PropertiesDelegate delegate) {
        return new PropertiesResolver() {
            @Override
            public <T> T resolveProperty(String name, Class<T> type) {
                return resolveProperty(name, null, type);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T resolveProperty(String name, T defaultValue) {
                return resolveProperty(name, defaultValue, (Class<T>) defaultValue.getClass());
            }

            private <T> T resolveProperty(final String name, Object defaultValue, final Class<T> type) {
                // Check runtime configuration first
                Object result = configuration.getProperty(name);
                if (result != null) {
                    defaultValue = result;
                }

                // Check request properties next
                result = delegate.getProperty(name);
                if (result == null) {
                    result = defaultValue;
                }

                return (result == null) ? null : PropertiesHelper.convertValue(result, type);
            }
        };
    }
}