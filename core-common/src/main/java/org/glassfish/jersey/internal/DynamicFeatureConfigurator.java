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

import org.glassfish.jersey.AbstractFeatureConfigurator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.DynamicFeature;
import java.util.List;
import java.util.Map;

/**
 * Registers JAX-RS {@link DynamicFeature} which are listed as SPIs for registration.
 * Also checks if JAX-RS service loading is enabled by the jakarta.ws.rs.loadServices property. In order for
 * registration to proceed the property shall be true (or null).
 *
 * Configurator is used only at Server side.
 *
 * This configurator's instance shall be after {@link AutoDiscoverableConfigurator}
 * in the list of configurators due to same list of {@link org.glassfish.jersey.internal.spi.AutoDiscoverable}
 * used in the {@link BootstrapBag} to register discovered features.
 */
public class DynamicFeatureConfigurator extends AbstractFeatureConfigurator<DynamicFeature> {

    /**
     * Create a new configurator.
     *
     * Must be used at server side only (takes no effect as a client).
     */
    public DynamicFeatureConfigurator() {
        super(DynamicFeature.class, RuntimeType.SERVER);
    }

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        final Map<String, Object> properties = bootstrapBag.getConfiguration().getProperties();
        if (PropertiesHelper.isJaxRsServiceLoadingEnabled(properties)) {
            final List<Class<DynamicFeature>> dynamicFeatures = loadImplementations(properties);
            dynamicFeatures.addAll(loadImplementations(properties, DynamicFeature.class.getClassLoader()));

            registerFeatures(dynamicFeatures, bootstrapBag);
        }
    }
}
