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
import static org.junit.Assert.assertThat;

/**
 * Test for the echo with injected param managed bean resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class EchoParamBeanTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"alpha", "beta"},
                {"AAA", "BBB"},
                {"b", "a"},
                {"1$s", "2&d"}
        });
    }

    final String a, b;

    /**
     * Create a new test case based on the above defined parameters.
     *
     * @param a query parameter value
     * @param b path parameter value
     */
    public EchoParamBeanTest(String a, String b) {
        this.a = a;
        this.b = b;
    }

    @Test
    public void testEchoParamResource() {

        final WebTarget target = target().path("echofield").path(b);

        String s = target.queryParam("a", a).request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(String.format("%s %s", a, b)));
    }
}
