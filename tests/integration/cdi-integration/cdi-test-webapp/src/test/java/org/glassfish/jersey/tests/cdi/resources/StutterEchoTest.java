/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for qualified injection.
 *
 * @author Jakub Podlesak
 */
@RunWith(Parameterized.class)
public class StutterEchoTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][]{
            {"alpha", "alphaalpha"},
            {"gogol", "gogolgogol"},
            {"elcaro", "elcaroelcaro"}
        });
    };

    final String in, out;

    /**
     * Construct instance with the above test data injected.
     *
     * @param in query parameter.
     * @param out expected output.
     */
    public StutterEchoTest(String in, String out) {
        this.in = in;
        this.out = out;
    }

    @Test
    public void testGet() {
        String s = target().path("stutter").queryParam("s", in).request().get(String.class);
        assertThat(s, equalTo(out));
    }
}
