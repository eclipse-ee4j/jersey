/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;

/**
 * Wrapper of {@link javax.ws.rs.core.FeatureContext} that can supply instance of
 * {@link InjectionManager injection manager}.
 *
 * @author Miroslav Fuksa
 */
public class FeatureContextWrapper implements FeatureContext, InjectionManagerSupplier {

    private final FeatureContext context;
    private final InjectionManager injectionManager;

    /**
     * Create a new instance of wrapper.
     *
     * @param context     Feature context instance that should be wrapped.
     * @param injectionManager injection manager.
     */
    public FeatureContextWrapper(FeatureContext context, InjectionManager injectionManager) {
        this.context = context;
        this.injectionManager = injectionManager;
    }

    @Override
    public Configuration getConfiguration() {
        return context.getConfiguration();
    }

    @Override
    public FeatureContext property(String name, Object value) {
        return context.property(name, value);
    }

    @Override
    public FeatureContext register(Class<?> componentClass) {
        return context.register(componentClass);
    }

    @Override
    public FeatureContext register(Class<?> componentClass, int priority) {
        return context.register(componentClass, priority);
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Class<?>... contracts) {
        return context.register(componentClass, contracts);
    }

    @Override
    public FeatureContext register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return context.register(componentClass, contracts);
    }

    @Override
    public FeatureContext register(Object component) {
        return context.register(component);
    }

    @Override
    public FeatureContext register(Object component, int priority) {
        return context.register(component, priority);
    }

    @Override
    public FeatureContext register(Object component, Class<?>... contracts) {
        return context.register(component, contracts);
    }

    @Override
    public FeatureContext register(Object component, Map<Class<?>, Integer> contracts) {
        return context.register(component, contracts);
    }

    @Override
    public InjectionManager getInjectionManager() {
        return injectionManager;
    }
}
