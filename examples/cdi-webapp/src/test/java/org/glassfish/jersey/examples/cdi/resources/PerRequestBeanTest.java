/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import java.util.stream.Stream;

import javax.ws.rs.client.WebTarget;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the request scoped managed bean resource.
 *
 * @author Jakub Podlesak
 */
public class PerRequestBeanTest extends CdiTest {

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("gamma", "delta"),
                Arguments.of("CC C", "D DD"),
                Arguments.of("d", "c"),
                Arguments.of("@^&", "?!:")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testTheOtherResource(String c, String d) {
        final WebTarget target = target().path("other").path(c).path(d);

        String s = target.request().get(String.class);
        assertThat(s, containsString(target.getUri().toString()));
        assertThat(s, containsString("GET"));
        assertThat(s, containsString(String.format("c=%s", c)));
        assertThat(s, containsString(String.format("d=%s", d)));
        assertThat(s, startsWith("INTERCEPTED"));
    }
}
