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

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Test for the helloworld resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class HelloworldTest extends CdiTest {

    @Test
    public void testHelloworldResource() {
        String s = target().path("helloworld").request().get(String.class);
        assertThat(s, containsString("Hello World"));
    }
}
