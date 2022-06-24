/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources.test;

import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the request scoped resource.
 *
 * @author Jakub Podlesak
 * @author Patrik Dudits
 */
public class PerRequestBeanTest extends CdiTest {

    public static Stream<String> testData() {
        return Stream.of("alpha", "AAA", "$%^", "a b");
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testGet(String x) {

        final WebTarget target = target().path("jcdibean/per-request").queryParam("x", x);

        String s = target.request().get(String.class);

        assertThat(s, containsString(target.getUri().toString()));
        assertThat(s, containsString(String.format("queryParam=%s", x)));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testSingleResponseFilterInvocation(String x) {

        final WebTarget target = target().path("jcdibean/per-request").queryParam("x", x);

        Response response = target.request().get();

        List<Object> invocationIds = response.getHeaders().get("Filter-Invoked");

        assertNotNull(invocationIds, "Filter-Invoked header should be set by ResponseFilter");
        assertEquals(1, invocationIds.size(), "ResponseFilter should be invoked only once");
    }

}
