/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sysprops;

import java.util.Set;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * System properties example unit test.
 *
 * @author Martin Matula
 */
public class SysPropsTest extends JerseyTest {
    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return App.createApp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(PropertiesReader.class);
    }

    @Test
    public void testGetPropertyNames() {
        PropertyNamesResource propertyNamesResource = WebResourceFactory.newResource(PropertyNamesResource.class, target());
        Set<String> propertyNames = propertyNamesResource.getPropertyNames();
        assertEquals(System.getProperties().stringPropertyNames(), propertyNames);
    }

    @Test
    public void testGetProperty() {
        PropertyNamesResource pnr = WebResourceFactory.newResource(PropertyNamesResource.class, target());

        assertEquals(System.getProperty("java.home"), pnr.getProperty("java.home").get());
    }

    @Test
    public void testSetProperty() {
        PropertyNamesResource pnr = WebResourceFactory.newResource(PropertyNamesResource.class, target());

        pnr.getProperty("test").set("this is a test");
        assertEquals(System.getProperty("test"), pnr.getProperty("test").get());
    }
}
