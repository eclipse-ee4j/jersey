/*
 * Copyright (c) 2014, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test injection of request depending instances works as expected.
 *
 * @author Jakub Podlesak
 */
public class RequestSensitiveTest extends CdiTest {

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("app-field-injected", "alpha", "App: alpha"),
                Arguments.of("app-field-injected", "gogol", "App: gogol"),
                Arguments.of("app-field-injected", "elcaro", "App: elcaro"),
                Arguments.of("app-ctor-injected", "alpha", "App: alpha"),
                Arguments.of("app-ctor-injected", "gogol", "App: gogol"),
                Arguments.of("app-ctor-injected", "elcaro", "App: elcaro"),
                Arguments.of("request-field-injected", "alpha", "Request: alpha"),
                Arguments.of("request-field-injected", "gogol", "Request: gogol"),
                Arguments.of("request-field-injected", "oracle", "Request: oracle"),
                Arguments.of("request-ctor-injected", "alpha", "Request: alpha"),
                Arguments.of("request-ctor-injected", "gogol", "Request: gogol"),
                Arguments.of("request-ctor-injected", "oracle", "Request: oracle")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testCdiInjection(String resource, String straight, String echoed) {
        final String s = target().path(resource).queryParam("s", straight).request().get(String.class);
        assertThat(s, equalTo(echoed));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testHk2Injection(String resource, String straight, String echoed) {
        final String s = target().path(resource).path("path").path(straight).request().get(String.class);
        assertThat(s, equalTo(String.format("%s/path/%s", resource, straight)));
    }
}
