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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test usage of Java SE 8 lambdas in JAX-RS resource methods.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class LambdaResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new Java8Application();
    }

    /**
     * Test that JDK8 lambdas do work in common JAX-RS resource methods.
     */
    @Test
    public void testLambdas() {
        final WebTarget target = target("lambdas/{p}");

        // test default method with no @Path annotation
        String response = target.resolveTemplate("p", "test").request().get(String.class);
        assertThat(response, equalTo("test-lambdaized"));
    }

}
