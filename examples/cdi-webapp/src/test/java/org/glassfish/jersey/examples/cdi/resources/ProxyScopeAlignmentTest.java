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
 * Ensure CDI and JAX-RS scopes are well aligned, so that dynamic proxies
 * are only created when needed.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class ProxyScopeAlignmentTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"one"},
                {"too"},
                {"much"}
        });
    }

    final String p;

    /**
     * Create a new test case based on the above defined parameters.
     *
     * @param p path parameter value
     */
    public ProxyScopeAlignmentTest(String p) {
        this.p = p;
    }

    @Test
    public void testUiInjection() {

        final WebTarget app = target().path("ui-app").path(p);
        final WebTarget req = target().path("ui-req").path(p);

        String ar = app.request().get(String.class);
        String rr = req.request().get(String.class);

        assertThat(ar, containsString(p));
        assertThat(rr, containsString(p));
    }
}
