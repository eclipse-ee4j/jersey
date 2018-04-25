/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.model.internal;

import java.util.Map;

import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * Immutable runtime configuration.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ImmutableCommonConfig extends CommonConfig {

    private final String errorMessage;

    /**
     * Create new immutable copy of the original runtime configuration
     * with a custom modification error message.
     *
     * @param config original runtime configuration.
     * @param modificationErrorMessage custom modification error message.
     */
    public ImmutableCommonConfig(final CommonConfig config, final String modificationErrorMessage) {
        super(config);

        this.errorMessage = modificationErrorMessage;
    }

    /**
     * Create new immutable copy of the original runtime configuration.
     *
     * @param config original runtime configuration.
     */
    public ImmutableCommonConfig(final CommonConfig config) {
        this(config, LocalizationMessages.CONFIGURATION_NOT_MODIFIABLE());
    }

    @Override
    public ImmutableCommonConfig property(String name, Object value) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig setProperties(final Map<String, ?> properties) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Class<?> componentClass) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Class<?> componentClass, final int bindingPriority) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Class<?> componentClass, final Class<?>... contracts) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public CommonConfig register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Object component) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Object component, final int bindingPriority) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ImmutableCommonConfig register(final Object component, final Class<?>... contracts) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public CommonConfig register(Object component, Map<Class<?>, Integer> contracts) {
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public CommonConfig loadFrom(Configuration config) {
        throw new IllegalStateException(errorMessage);
    }
}
