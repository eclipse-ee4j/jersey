/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.AcceptableMediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Acceptable media type unit tests.
 *
 * @author Marek Potociar
 */
public class AcceptableMediaTypeTest {
    // expected result, media type, acceptable media type
    public static Stream<Arguments> testBeds() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE, new AcceptableMediaType("application", "json")),
                Arguments.of(Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE,
                        new AcceptableMediaType("application", "json", 1000, null)),
                Arguments.of(Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE,
                        new AcceptableMediaType("application", "json", 500, null)),
                Arguments.of(Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE, new AcceptableMediaType("application", "xml"))
        );
    }

    @ParameterizedTest
    @MethodSource("testBeds")
    public void testEquals(boolean expectEquality, MediaType mediaType,
            AcceptableMediaType acceptableMediaType) throws Exception {
        if (expectEquality) {
            Assertions.assertEquals(mediaType, acceptableMediaType, "Types not equal.");
            Assertions.assertEquals(acceptableMediaType, mediaType, "Types not equal.");
            Assertions.assertEquals(mediaType.hashCode(), acceptableMediaType.hashCode(),
                    String.format("Hash codes not equal for %s and %s.", mediaType.toString(), acceptableMediaType.toString()));
        } else {
            Assertions.assertFalse(acceptableMediaType.equals(mediaType),
                    String.format("False equality of %s and %s", mediaType.toString(), acceptableMediaType.toString()));
        }
    }
}
