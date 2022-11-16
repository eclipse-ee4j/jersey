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

import org.glassfish.jersey.message.internal.QualitySourceMediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Quality source media type unit tests.
 *
 * @author Marek Potociar
 */
public class QualitySourceMediaTypeTest {
    // expected result, media type, quality source media type
    public static Stream<Arguments> testBeds() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "json")),
                Arguments.of(Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE,
                        new QualitySourceMediaType("application", "json", 1000, null)),
                Arguments.of(Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE,
                        new QualitySourceMediaType("application", "json", 500, null)),
                Arguments.of(Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "xml"))
        );
    }

    @ParameterizedTest
    @MethodSource("testBeds")
    public void testEquals(boolean expectEquality, MediaType mediaType, QualitySourceMediaType qsMediaType) throws Exception {
        if (expectEquality) {
            Assertions.assertEquals(mediaType, qsMediaType, "Types not equal.");
            Assertions.assertEquals(qsMediaType, mediaType, "Types not equal.");
            Assertions.assertEquals(mediaType.hashCode(), qsMediaType.hashCode(),
                    String.format("Hash codes not equal for %s and %s.", mediaType.toString(), qsMediaType.toString()));
        } else {
            Assertions.assertFalse(qsMediaType.equals(mediaType),
                    String.format("False equality of %s and %s", mediaType.toString(), qsMediaType.toString()));
        }
    }
}
