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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.WebTarget;

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
@RunWith(Parameterized.class)
public class PerRequestBeanTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"gamma", "delta"},
                {"CC C", "D DD"},
                {"d", "c"},
                {"@^&", "?!:"}
        });
    }

    final String c, d;

    /**
     * Create a new test case based on the above defined parameters.
     *
     * @param c first path parameter value.
     * @param d second path parameter value.
     */
    public PerRequestBeanTest(String c, String d) {
        this.c = c;
        this.d = d;
    }

    @Test
    public void testTheOtherResource() {
        final WebTarget target = target().path("other").path(c).path(d);

        String s = target.request().get(String.class);
        assertThat(s, containsString(target.getUri().toString()));
        assertThat(s, containsString("GET"));
        assertThat(s, containsString(String.format("c=%s", c)));
        assertThat(s, containsString(String.format("d=%s", d)));
        assertThat(s, startsWith("INTERCEPTED"));
    }
}
