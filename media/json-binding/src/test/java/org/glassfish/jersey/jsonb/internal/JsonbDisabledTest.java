/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jsonb.internal;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.FeatureContext;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class JsonbDisabledTest {
    @Test
    public void testDisabled() {
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        config2.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE, true);
        configReference.set(config2);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));
    }

    @Test
    public void testDisabledBySystemProperty() {
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER, "true");
        configReference.set(config2);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER);
    }

    @Test
    public void testDisabledBySystemPropertyOverridenByConfigProperty() {
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE, "true");
        config2.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE, false);
        configReference.set(config2);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE);
    }

    @Test
    public void testDisabledByPropertyApplicationPackage() {
        Application application = new Application() {
        };
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(application).to(Application.class);
            }
        });
        AtomicReference<InjectionManager> injectionManagerReference = new AtomicReference<>(injectionManager);
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference, injectionManagerReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION,
                "some.does.not.matter, org.glassfish.jersey.jsonb.internal");
        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config2);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION);
    }

    @Test
    public void disableOnClientOnlyTest() {
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.CLIENT, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        config1.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER, true);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        config1.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE_CLIENT, true);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));

        CommonConfig config2 = new CommonConfig(RuntimeType.CLIENT, ComponentBag.INCLUDE_ALL);
        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER, "true");
        configReference.set(config2);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_CLIENT, "true");
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));

        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER);
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_CLIENT);
    }

    @Test
    public void testDisabledBySystemPropertyApplicationPackage() {
        Application application = new Application() {
        };
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(application).to(Application.class);
            }
        });
        AtomicReference<InjectionManager> injectionManagerReference = new AtomicReference<>(injectionManager);
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference, injectionManagerReference);

        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION,
                "some.does.not.matter, org.glassfish.jersey.jsonb.internal");

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));

        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config2);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION);
    }

    @Test
    public void testDisabledBySystemPropertyApplicationPackageEnabledByProperty() {
        Application application = new Application() {
        };
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(application).to(Application.class);
            }
        });
        AtomicReference<InjectionManager> injectionManagerReference = new AtomicReference<>(injectionManager);
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = featureContextForConfig(configReference, injectionManagerReference);

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));

        System.setProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION,
                "some.does.not.matter, org.glassfish.jersey.jsonb.internal");

        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        config2.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE, Boolean.FALSE);
        configReference.set(config2);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));
        System.clearProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION);
    }

    private static FeatureContext featureContextForConfig(AtomicReference<CommonConfig> configReference) {
        return featureContextForConfig(configReference, new AtomicReference<>());
    }
    private static FeatureContext featureContextForConfig(AtomicReference<CommonConfig> configReference,
                                                          AtomicReference<InjectionManager> injectionManager) {
        return (FeatureContext) Proxy.newProxyInstance(
                JsonbDisabledTest.class.getClassLoader(),
                new Class[] {FeatureContext.class, InjectionManagerSupplier.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getConfiguration":
                            return configReference.get();
                        case "getInjectionManager":
                            return injectionManager.get();
                    }
                    return null;
                });
    }
}
