/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.util.stream.Stream;

import jakarta.ws.rs.client.WebTarget;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the request scoped managed bean resource.
 *
 * @author Jakub Podlesak
 */
public class PerRequestDependentBeanTest extends CdiTest {

    public static Stream<String> testData() {
        return Stream.of("alpha", "AAA", "$%^", "a b");
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testGet(String x) {

        final WebTarget target = target().path("jcdibean/dependent/per-request").queryParam("x", x);

        String s = target.request().get(String.class);

        assertThat(s, containsString(target.getUri().toString()));
        assertThat(s, containsString(String.format("queryParam=%s", x)));
    }
}
