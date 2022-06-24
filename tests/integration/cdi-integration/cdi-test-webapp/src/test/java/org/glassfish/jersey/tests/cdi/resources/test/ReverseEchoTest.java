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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for qualified injection.
 *
 * @author Jakub Podlesak
 */
public class ReverseEchoTest extends CdiTest {

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("alpha", "ahpla"),
                Arguments.of("gogol", "logog"),
                Arguments.of("elcaro", "oracle")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testGet(String in, String out) {
        WebTarget reverseService = target().path("reverse").queryParam("s", in);
        String s = reverseService.request().get(String.class);
        assertThat(s, equalTo(out));
    }
}
