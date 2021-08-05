/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.Feature;
import java.lang.reflect.Array;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registers JAX-RS {@link Feature} and {@link DynamicFeature} which are listed as SPIs for registration.
 * Also checks if JAX-RS service loading is enabled by the javax.ws.rs.loadServices property. In order for
 * registration to proceed the property shall be true (or null).
 *
 * This configurator's instance shall be the last (or at least after {@link AutoDiscoverableConfigurator})
 * in the list of configurators due to same list of {@link org.glassfish.jersey.internal.spi.AutoDiscoverable}
 * used in the {@link BootstrapBag} to register discovered features.
 */
public class FeatureConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        if (PropertiesHelper.isJaxRsServiceLoadingEnabled(bootstrapBag.getConfiguration().getProperties())) {
            final List<Class<Feature>> features = discoverFeatures(injectionManager, Feature.class);
            final List<Class<DynamicFeature>> dynamicFeatures = discoverFeatures(injectionManager, DynamicFeature.class);
            registerFeatures(features, dynamicFeatures, bootstrapBag);
        }
    }

    private static <T> List<Class<T>> discoverFeatures(InjectionManager injectionManager, Class<T> registrationType) {
        final List<Class<T>> result = Stream.of(discoverFeaturesByClassLoader(registrationType))
                .peek(implClass -> injectionManager.register(Bindings.service(implClass).to(registrationType)))
//                .map(injectionManager::createAndInitialize)
                .collect(Collectors.toList());

        return result;
    }

    private static <T> Class<T>[] discoverFeaturesByClassLoader(Class<T> registrationType) {
        final ClassLoader contextClassLoader =
                AccessController.doPrivileged(ReflectionHelper.getContextClassLoaderPA());
        final ClassLoader registrationTypeClassLoader =
                AccessController.doPrivileged(ReflectionHelper.getClassLoaderPA(registrationType));
        final ServiceFinder<T> serviceFinder = ServiceFinder.find(registrationType, true);
        final List<Class<T>> results = Arrays.asList(serviceFinder.toClassArray());
        if (!contextClassLoader.equals(registrationTypeClassLoader)) {
            final ServiceFinder<T> finder =
                    ServiceFinder.find(registrationType, registrationTypeClassLoader, true);
            results.addAll(Arrays.asList(finder.toClassArray()));
        }
        return results.toArray((Class<T>[]) Array.newInstance(Class.class, results.size()));
    }

    private static void registerFeatures(List<Class<Feature>> features,
                                         List<Class<DynamicFeature>> dynamicFeatures,
                                         BootstrapBag bootstrapBag) {
        final List<ForcedAutoDiscoverable> forcedAutoDiscoverables = new ArrayList<>();

        features.forEach(feature -> forcedAutoDiscoverables.add(registerClass(feature)));
        dynamicFeatures.forEach(dynamicFeature -> forcedAutoDiscoverables.add(registerClass(dynamicFeature)));

        bootstrapBag.getAutoDiscoverables().addAll(forcedAutoDiscoverables);
    }

    private static <T> ForcedAutoDiscoverable registerClass(Class<T> classToRegister) {
        return context -> {
            if (!context.getConfiguration().isRegistered(classToRegister)) {
                context.register(classToRegister, AutoDiscoverable.DEFAULT_PRIORITY);
            }
        };
    }

    @Override
    public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        BootstrapConfigurator.super.postInit(injectionManager, bootstrapBag);
    }
}
