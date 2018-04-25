/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Test injection of request depending instances works as expected.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class RequestSensitiveTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"app-field-injected", "alpha", "App: alpha"},
                {"app-field-injected", "gogol", "App: gogol"},
                {"app-field-injected", "elcaro", "App: elcaro"},
                {"app-ctor-injected", "alpha", "App: alpha"},
                {"app-ctor-injected", "gogol", "App: gogol"},
                {"app-ctor-injected", "elcaro", "App: elcaro"},
                {"request-field-injected", "alpha", "Request: alpha"},
                {"request-field-injected", "gogol", "Request: gogol"},
                {"request-field-injected", "oracle", "Request: oracle"},
                {"request-ctor-injected", "alpha", "Request: alpha"},
                {"request-ctor-injected", "gogol", "Request: gogol"},
                {"request-ctor-injected", "oracle", "Request: oracle"}
        });
    }

    final String resource, straight, echoed;

    /**
     * Construct instance with the above test data injected.
     *
     * @param resource uri of the resource to be tested.
     * @param straight request specific input.
     * @param echoed   CDI injected service should produce this out of previous, straight, parameter.
     */
    public RequestSensitiveTest(final String resource, final String straight, final String echoed) {
        this.resource = resource;
        this.straight = straight;
        this.echoed = echoed;
    }

    @Test
    public void testCdiInjection() {
        final String s = target().path(resource).queryParam("s", straight).request().get(String.class);
        assertThat(s, equalTo(echoed));
    }

    @Test
    public void testHk2Injection() {
        final String s = target().path(resource).path("path").path(straight).request().get(String.class);
        assertThat(s, equalTo(String.format("%s/path/%s", resource, straight)));
    }
}
