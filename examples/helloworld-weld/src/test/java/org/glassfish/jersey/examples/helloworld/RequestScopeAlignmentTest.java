/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.jboss.weld.environment.se.Weld;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Test for the request scoped managed bean resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class RequestScopeAlignmentTest extends JerseyTest {

    static Weld weld;

    @BeforeClass
    public static void before() throws Exception {
        weld = new Weld();
        weld.initialize();
    }

    @AfterClass
    public static void after() throws Exception {
        weld.shutdown();
    }

    @Override
    protected ResourceConfig configure() {
        return App.createJaxRsApp();
    }

    @Test
    public void testUriInfoPropagatesToApp() {

        for (String d : new String[]{"one", "two", "three"}) {

            final WebTarget fieldTarget = target().path("req/ui/jax-rs-field").path(d);
            final WebTarget appFieldTarget = target().path("req/ui/jax-rs-app-field").path(d);

            String f = fieldTarget.request().get(String.class);
            assertThat(f, containsString(fieldTarget.getUri().toString()));
            String af = appFieldTarget.request().get(String.class);
            assertThat(af, containsString(appFieldTarget.getUri().toString()));
        }
    }
}
