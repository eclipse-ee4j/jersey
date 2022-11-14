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
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the echo with injected param managed bean resource.
 *
 * @author Jakub Podlesak
 */
public class EchoParamBeanTest extends CdiTest {

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("alpha", "beta"),
                Arguments.of("AAA", "BBB"),
                Arguments.of("b", "a"),
                Arguments.of("1$s", "2&d")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testEchoParamResource(String a, String b) {

        final WebTarget target = target().path("echofield").path(b);

        String s = target.queryParam("a", a).request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(String.format("%s %s", a, b)));
    }
}
