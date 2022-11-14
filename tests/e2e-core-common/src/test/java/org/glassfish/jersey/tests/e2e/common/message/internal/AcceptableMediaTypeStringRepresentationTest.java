/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.MediaTypeProvider;
import org.glassfish.jersey.message.internal.Quality;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Acceptable media type unit tests.
 *
 * @author Adam Lindenthal
 */
public class AcceptableMediaTypeStringRepresentationTest {
    // expected result, acceptable media type
    public static Stream<Arguments> getParameters() {
        final Map<String, String> emptyParams = new HashMap<String, String>();
        final Map<String, String> params = new HashMap<String, String>();
        params.put("myParam", "myValue");

        return Stream.of(
                Arguments.of("*/*", new AcceptableMediaType("*", "*")),
                Arguments.of("*/*", new AcceptableMediaType("*", "*", Quality.DEFAULT, emptyParams)),
                Arguments.of("*/*;q=0.75", new AcceptableMediaType("*", "*", 750, emptyParams)),
                Arguments.of("text/html", new AcceptableMediaType("text", "html", Quality.DEFAULT, null)),
                Arguments.of("text/html;q=0.5", new AcceptableMediaType("text", "html", 500, emptyParams)),
                Arguments.of("image/*;myparam=myValue;q=0.8", new AcceptableMediaType("image", "*", 800, params))
        );
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void testStringRepresentation(String expectedValue, AcceptableMediaType testedType) {
        final MediaTypeProvider provider = new MediaTypeProvider();
        Assertions.assertEquals(expectedValue, testedType.toString());
        provider.fromString(testedType.toString());
    }
}
