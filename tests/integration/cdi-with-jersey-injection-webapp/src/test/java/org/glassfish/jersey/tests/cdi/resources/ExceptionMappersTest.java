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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for exception mapper injection.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class ExceptionMappersTest extends CdiTest {

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
    public ExceptionMappersTest(String resource) {
        this.resource = resource;
    }

    /**
     * Check that for one no NPE happens on the server side, and for two
     * the injected mappers remains the same across requests.
     */
    @Test
    public void testMappersNotNull() {
        WebTarget target = target().path(resource).path("mappers");
        final Response firstResponse = target.request().get();
        assertThat(firstResponse.getStatus(), equalTo(200));
        String firstValue = firstResponse.readEntity(String.class);
        assertThat(target.request().get(String.class), equalTo(firstValue));
    }
}
