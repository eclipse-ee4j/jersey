/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Abstract entity provider model.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.16
 */
public abstract class AbstractEntityProviderModel<T> {

    private final T provider;
    private final List<MediaType> declaredTypes;
    private final boolean custom;
    private final Class<?> providedType;

    /**
     * Create new entity provider model.
     *
     * NOTE: The constructor is package private on purpose as we do not support extensions of this class from another package.
     *
     * @param provider      entity provider instance.
     * @param declaredTypes declared supported media types.
     * @param custom        custom flag; {@code true} is the provider is custom, {@code false} if the provider is one of the
     *                      default Jersey providers.
     * @param providerType  parameterized entity provider type (used to retrieve the provided Java type).
     */
    AbstractEntityProviderModel(final T provider,
                                final List<MediaType> declaredTypes,
                                final boolean custom,
                                final Class<T> providerType) {
        this.provider = provider;
        this.declaredTypes = declaredTypes;
        this.custom = custom;
        this.providedType = getProviderClassParam(provider, providerType);
    }

    /**
     * Get the modelled entity provider instance.
     *
     * @return entity provider instance.
     */
    public T provider() {
        return provider;
    }

    /**
     * Get types declared as supported (via {@code @Produces} or {@code @Consumes}) on the entity provider.
     *
     * @return declared supported types.
     */
    public List<MediaType> declaredTypes() {
        return declaredTypes;
    }

    /**
     * Get the {@code custom} flag value.
     *
     * @return {@code true} if the provider is a custom implementation, {@code false} if the provider is
     * one of the default providers supplied with Jersey.
     */
    public boolean isCustom() {
        return custom;
    }

    /**
     * Get the provided Java type.
     *
     * @return provided Java type.
     */
    public Class<?> providedType() {
        return providedType;
    }

    private static Class<?> getProviderClassParam(Object provider, Class<?> providerType) {
        final ReflectionHelper.DeclaringClassInterfacePair pair =
                ReflectionHelper.getClass(provider.getClass(), providerType);
        final Class[] classArgs = ReflectionHelper.getParameterizedClassArguments(pair);

        return classArgs != null ? classArgs[0] : Object.class;
    }
}
