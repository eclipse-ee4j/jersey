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
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the echo resource.
 *
 * @author Jakub Podlesak
 */
public class EchoResourceTest extends CdiTest {

    public static Stream<String> testData() {
        return Stream.of("alpha", "AAA", "b", "1");
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testEchoResource(String a) {

        final WebTarget target = target().path("echo").path(a);

        String s = target.request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(a));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testEchoParamCtorResource(String a) {

        final WebTarget target = target().path("echoparamconstructor").path(a);

        String s = target.request().get(String.class);

        assertThat(s, containsString("ECHO"));
        assertThat(s, containsString(a));
    }
}
