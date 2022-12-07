/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.config;

import org.glassfish.jersey.CommonProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

public class AdditionalSystemPropertiesTest {

    private static class AdditionalSystemProperties {
        public static final String FIRST_PROPERTY = "first.property";
        public static final String SECOND_PROPERTY = "second.property";
    }

    @BeforeAll
    public static void setUp() {
        System.setProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.TRUE.toString());
        System.getProperties().put(AdditionalSystemProperties.FIRST_PROPERTY, "first value");
        System.getProperties().put(AdditionalSystemProperties.SECOND_PROPERTY, "second value");
    }

    @AfterAll
    public static void tearDown() {
        System.clearProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER);
        System.clearProperty(AdditionalSystemProperties.FIRST_PROPERTY);
        System.clearProperty(AdditionalSystemProperties.SECOND_PROPERTY);
    }

    @Test
    public void readAdditionalSystemPropertiesTest() {
        SystemPropertiesConfigurationModel testModel = new SystemPropertiesConfigurationModel(
                Collections.singletonList(AdditionalSystemProperties.class.getName())
        );

        Properties properties = new Properties();
        ExternalPropertiesConfigurationFactory.configure((k, v) -> properties.put(k, v),
                Collections.singletonList(new ExternalConfigurationProviderImpl(testModel))
        );

        Assertions.assertFalse(properties.isEmpty());
        Assertions.assertTrue(properties.containsKey(AdditionalSystemProperties.FIRST_PROPERTY));
        Assertions.assertTrue(properties.containsKey(AdditionalSystemProperties.SECOND_PROPERTY));
        Assertions.assertEquals("first value", properties.get(AdditionalSystemProperties.FIRST_PROPERTY));
        Assertions.assertEquals("second value", properties.get(AdditionalSystemProperties.SECOND_PROPERTY));
    }
}
