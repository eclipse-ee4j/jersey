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
 * Tests CDI counter resource.
 */
public class CounterTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(CounterResource.class);
    }

    @Test
    public void testRequestCounter() throws InterruptedException {
        Integer response1 = target().path(App.ROOT_COUNTER_PATH).path("request").request().get(Integer.class);
        Integer response2 = target().path(App.ROOT_COUNTER_PATH).path("request").request().get(Integer.class);
        assertEquals((Integer) 1, response1);
        assertEquals((Integer) 1, response2);
    }

    @Test
    public void testApplicationCounter() throws InterruptedException {
        Integer response1 = target().path(App.ROOT_COUNTER_PATH).path("application").request().get(Integer.class);
        Integer response2 = target().path(App.ROOT_COUNTER_PATH).path("application").request().get(Integer.class);
        assertEquals((Integer) 1, response1);
        assertEquals((Integer) 2, response2);
    }
}
