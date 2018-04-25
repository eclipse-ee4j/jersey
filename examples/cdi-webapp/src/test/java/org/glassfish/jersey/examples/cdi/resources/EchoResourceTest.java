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
 * Test for the echo resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class EchoResourceTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"alpha"},
                {"AAA"},
                {"b"},
                {"1"}
        });
    }

    final String a;

    /**
     * Create a new test case based on the above defined parameters.
     *
     * @param a path parameter value
     */
    public EchoResourceTest(String a) {
        this.a = a;
    }

    @Test
    public void testEchoResource() {

        final WebTarget target = target().path("echo").path(a);

        String s = target.request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(a));
    }

    @Test
    public void testEchoParamCtorResource() {

        final WebTarget target = target().path("echoparamconstructor").path(a);

        String s = target.request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(a));
    }
}
