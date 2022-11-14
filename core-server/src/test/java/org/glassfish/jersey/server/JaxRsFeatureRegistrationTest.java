/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import org.glassfish.jersey.CommonProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class JaxRsFeatureRegistrationTest {

    public static class FeatureImpl implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            return true;
        }
    }

    public static class DynamicFeatureImpl implements DynamicFeature {

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        }
    }

    @Test
    public void featureRegistrationTest() {
        final ResourceConfig config = new ResourceConfig();
        final ApplicationHandler ah = new ApplicationHandler(config);

        Assertions.assertTrue(ah.getConfiguration().isRegistered(FeatureImpl.class));
        Assertions.assertTrue(ah.getConfiguration().isRegistered(DynamicFeatureImpl.class));
    }

    @Test
    public void serviceLoadingPropertyTest() {

        final ResourceConfig config = new ResourceConfig()
                .property(CommonProperties.JAXRS_SERVICE_LOADING_ENABLE, "false");
        final ApplicationHandler ah = new ApplicationHandler(config);

        Assertions.assertFalse(ah.getConfiguration().isRegistered(FeatureImpl.class));
        Assertions.assertFalse(ah.getConfiguration().isRegistered(DynamicFeatureImpl.class));
    }
}