/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.glassfish.jersey.internal.config.ExternalPropertiesConfigurationFactory.getConfig;
import static org.glassfish.jersey.internal.config.ExternalPropertiesConfigurationFactory.readExternalPropertiesMap;

public class ExternalPropertiesConfigurationFactoryTest {

    /**
     * Predefine some properties to be read from config
     */
    @BeforeClass
    public static void setUp() {
        System.setProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.TRUE.toString());

        System.setProperty("jersey.config.server.provider.scanning.recursive", "PASSED");
        System.setProperty(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, "1");
        System.setProperty("jersey.config.client.readTimeout", "10");
    }

    @AfterClass
    public static void tearDown() {
        System.clearProperty("jersey.config.server.provider.scanning.recursive");
        System.clearProperty(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE);
        System.clearProperty("jersey.config.client.readTimeout");
    }

    @Test
    public void readSystemPropertiesTest() {
        final Object result =
                readExternalPropertiesMap().get("jersey.config.server.provider.scanning.recursive");
        Assert.assertNull(result);
        Assert.assertEquals(Boolean.TRUE,
                getConfig().as(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, Boolean.class));
        Assert.assertEquals(Boolean.FALSE,
                getConfig().as("jersey.config.client.readTimeout", Boolean.class));
        Assert.assertEquals(1,
                getConfig().as(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, Integer.class));
        Assert.assertEquals(10,
                getConfig().as("jersey.config.client.readTimeout", Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedMapperTest() {
        getConfig().as(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, Double.class);
    }

    @Test
    public void mergePropertiesTest() {
        final Map<String, Object> inputProperties = new HashMap<>();
        inputProperties.put("jersey.config.server.provider.scanning.recursive", "MODIFIED");
        inputProperties.put("org.jersey.microprofile.config.added", "ADDED");
        getConfig().mergeProperties(inputProperties);
        final Object result = readExternalPropertiesMap().get("jersey.config.server.provider.scanning.recursive");
        Assert.assertNull(result);
        Assert.assertNull(readExternalPropertiesMap().get("org.jersey.microprofile.config.added"));
    }

}
