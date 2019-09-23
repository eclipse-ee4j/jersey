/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test custom HK2 injection.
 *
 * @author Jakub Podlesak
 */
@RunWith(Parameterized.class)
public class CustomInjectionTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"app-field-injected"},
                {"app-ctor-injected"},
                {"request-field-injected"},
                {"request-ctor-injected"}
        });
    }

    final String resource;

    /**
     * Construct instance with the above test data injected.
     *
     * @param resource query parameter.
     */
    public CustomInjectionTest(String resource) {
        this.resource = resource;
    }

    /**
     * Check that for one no NPE happens on the server side,
     * and the custom bound instance gets injected.
     */
    @Test
    public void testCustomHk2InjectionNull() {
        WebTarget target = target().path(resource).path("custom");
        final Response response = target.request().get();
        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("no way CDI would chime in"));
    }
}
