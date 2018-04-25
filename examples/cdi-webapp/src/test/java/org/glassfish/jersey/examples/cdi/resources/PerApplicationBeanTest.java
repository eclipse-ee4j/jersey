/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Test for the application scoped managed bean resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class PerApplicationBeanTest extends CdiTest {

    @Test
    public void testApplicationScopedResource() {

        final WebTarget singleton = target().path("singleton");

        String s = singleton.request().get(String.class);
        assertThat(s, containsString(singleton.getUri().toString()));
        assertThat(s, containsString("GET"));

        final WebTarget counter = singleton.path("counter");

        // TODO: JERSEY-2744:
        // TODO: @Resource injection will not work on SE
        // TODO: add a custom extension to make this work with Grizzly
//        String c42 = counter.request().get(String.class);
//        assertThat(c42, containsString("42"));
//
//        String c43 = counter.request().get(String.class);
//        assertThat(c43, containsString("43"));

        counter.request().put(Entity.text("12"));

        String c12 = counter.request().get(String.class);
        assertThat(c12, containsString("12"));

        counter.request().put(Entity.text("42"));
    }
}
