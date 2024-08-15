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
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class JsonbDisabledTest {
    @Test
    public void testDisabled() {
        AtomicReference<CommonConfig> configReference = new AtomicReference<>();
        FeatureContext featureContext1 = (FeatureContext) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {FeatureContext.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getConfiguration":
                            return configReference.get();
                    }
                    return null;
                });

        CommonConfig config1 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        configReference.set(config1);
        Assertions.assertTrue(new JsonBindingFeature().configure(featureContext1));


        CommonConfig config2 = new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL);
        config2.property(CommonProperties.JSON_BINDING_FEATURE_DISABLE, true);
        configReference.set(config2);
        Assertions.assertFalse(new JsonBindingFeature().configure(featureContext1));
    }
}
