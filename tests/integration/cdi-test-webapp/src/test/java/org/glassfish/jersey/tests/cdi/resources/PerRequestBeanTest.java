/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.cdi.resources;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.WebTarget;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test for the request scoped resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class PerRequestBeanTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"alpha"},
                {"AAA"},
                {"$%^"},
                {"a b"}
        });
    }

    final String x;

    /**
     * Create x new test case based on the above defined parameters.
     *
     * @param x query parameter value
     */
    public PerRequestBeanTest(String x) {
        this.x = x;
    }

    @Test
    public void testGet() {

        final WebTarget target = target().path("jcdibean/per-request").queryParam("x", x);

        String s = target.request().get(String.class);

        assertThat(s, containsString(target.getUri().toString()));
        assertThat(s, containsString(String.format("queryParam=%s", x)));
    }
}
