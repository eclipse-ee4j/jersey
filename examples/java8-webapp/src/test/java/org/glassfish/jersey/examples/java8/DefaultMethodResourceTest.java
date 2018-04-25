/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.java8;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test usage of Java8's interface default methods as resource methods.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class DefaultMethodResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new Java8Application();
    }

    /**
     * Test that JDK8 default methods do work as common JAX-RS resource methods.
     */
    @Test
    public void testDefaultMethods() {
        final WebTarget defaultMethodTarget = target("default-method");

        // test default method with no @Path annotation
        String response = defaultMethodTarget.request().get(String.class);
        assertEquals("interface-root", response);

        // test default method with with @Path annotation
        response = defaultMethodTarget.path("path").request().get(String.class);
        assertEquals("interface-path", response);
    }

    /**
     * Test, that resource methods defined in the class implementing the interface with default method do work normally.
     */
    @Test
    public void testImplementingClass() throws Exception {
        final String response = target("default-method").path("class").request().get(String.class);
        assertEquals("class", response);
    }
}
