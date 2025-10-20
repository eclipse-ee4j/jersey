/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey;

import org.glassfish.jersey.internal.AbstractServiceFinderConfigurator;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.RuntimeType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractFeatureConfigurator<T> extends AbstractServiceFinderConfigurator<T> {
    /**
     * Create a new configurator.
     *
     * @param contract    contract of the service providers bound by this binder.
     * @param runtimeType runtime (client or server) where the service finder binder is used.
     */
    protected AbstractFeatureConfigurator(Class contract, RuntimeType runtimeType) {
        super(contract, runtimeType);
    }

    /**
     * Specification specific implementation which allows find classes by specified classloader
     *
     * @param applicationProperties map of properties to check if search is allowed
     * @param loader specific classloader (must not be NULL)
     * @return list of found classes
     */

    protected List<Class<T>> loadImplementations(Map<String, Object> applicationProperties, ClassLoader loader) {
        if (PropertiesHelper.isMetaInfServicesEnabled(applicationProperties, getRuntimeType())) {
            return Stream.of(ServiceFinder.service(getContract()).loader(loader).ignoreNotFound(true)
                            .runtimeType(getRuntimeType()).find().toClassArray())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Allows feature registration as part of autoDiscoverables list
     *
     * @param features list of features to be registered
     * @param bootstrapBag place where features are being registered
     */
    protected void registerFeatures(Collection<Class<T>> features,
                                    BootstrapBag bootstrapBag) {
        final List<AutoDiscoverable> autoDiscoverables = new ArrayList<>();

        features.forEach(feature -> autoDiscoverables.add(registerClass(feature)));

        bootstrapBag.getAutoDiscoverables().addAll(autoDiscoverables);
    }

    /**
     * Register particular feature as an autoDiscoverable
     *
     * @param classToRegister class to be registered
     * @param <T> type of class which is being registered
     * @return initialized autoDiscoverable
     */
    private static <T> AutoDiscoverable registerClass(Class<T> classToRegister) {
        return context -> {
            if (!context.getConfiguration().isRegistered(classToRegister)) {
                context.register(classToRegister, AutoDiscoverable.DEFAULT_PRIORITY);
            }
        };
    }

}
