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

import java.util.Date;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.monitoring.ApplicationInfo;

/**
 * Application statistics.
 *
 * @author Miroslav Fuksa
 */
final class ApplicationInfoImpl implements ApplicationInfo {

    private final ResourceConfig resourceConfig;
    private final Date startTime;
    private final Set<Class<?>> registeredClasses;
    private final Set<Object> registeredInstances;
    private final Set<Class<?>> providers;

    /**
     * Create a new application statistics instance.
     *
     * @param resourceConfig Resource config of the application being monitored.
     * @param startTime Start time of the application (when initialization was finished).
     * @param registeredClasses Registered resource classes.
     * @param registeredInstances Registered resource instances.
     * @param providers Registered providers.
     */
    ApplicationInfoImpl(final ResourceConfig resourceConfig, final Date startTime, final Set<Class<?>> registeredClasses,
                        final Set<Object> registeredInstances, final Set<Class<?>> providers) {
        this.resourceConfig = resourceConfig;
        this.startTime = startTime;

        this.registeredClasses = registeredClasses;
        this.registeredInstances = registeredInstances;
        this.providers = providers;
    }

    @Override
    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    @Override
    public Date getStartTime() {
        return startTime;
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
    public ApplicationInfo snapshot() {
        // snapshot functionality not yet implemented
        return this;
    }

}
