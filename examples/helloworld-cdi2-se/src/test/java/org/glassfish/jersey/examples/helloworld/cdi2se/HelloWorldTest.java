/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests CDI helloworld resource.
 */
public class HelloWorldTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(HelloWorldResource.class);
    }

    @Test
    public void testHello() throws InterruptedException {
        String response = target().path(App.ROOT_HELLO_PATH).path("James").request().get(String.class);
        assertEquals("Hello James", response);
    }
}
