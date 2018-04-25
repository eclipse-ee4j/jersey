/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;

/**
 * Implementation of {@link ApplicationEvent application event}. Instances are immutable.
 *
 * @author Miroslav Fuksa
 */
public class ApplicationEventImpl implements ApplicationEvent {

    private final Type type;
    private final ResourceConfig resourceConfig;
    private final Set<Class<?>> providers;
    private final Set<Class<?>> registeredClasses;
    private final Set<Object> registeredInstances;
    private final ResourceModel resourceModel;

    /**
     * Create a new application event.
     * @param type Type of the event.
     * @param resourceConfig Resource config of the application.
     * @param registeredClasses Registered resource classes.
     * @param registeredInstances Registered resource instances.
     * @param resourceModel Resource model of the application (enhanced by
     *                      {@link org.glassfish.jersey.server.model.ModelProcessor model processors}).
     * @param providers Registered providers.
     */
    public ApplicationEventImpl(Type type, ResourceConfig resourceConfig,
                                Set<Class<?>> providers, Set<Class<?>> registeredClasses,
                                Set<Object> registeredInstances, ResourceModel resourceModel) {
        this.type = type;
        this.resourceConfig = resourceConfig;
        this.providers = providers;
        this.registeredClasses = registeredClasses;
        this.registeredInstances = registeredInstances;
        this.resourceModel = resourceModel;
    }

    @Override
    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Class<?>> getRegisteredClasses() {
        return registeredClasses;
    }

    @Override
    public Set<Object> getRegisteredInstances() {
        return registeredInstances;
    }

    @Override
    public Set<Class<?>> getProviders() {
        return providers;
    }

    @Override
    public ResourceModel getResourceModel() {
        return resourceModel;
    }
}
