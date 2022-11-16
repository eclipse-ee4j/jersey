/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class JdkVersionParseTest {

  public static Stream<Arguments> provideVersions() {
    return Stream.of(
        // Java 8
        Arguments.of("1.8.0_141-b15", 1, 8, 0, 141),
        Arguments.of("1.8.0_141", 1, 8, 0, 141),

        // Java 9 and above
        Arguments.of("9", 9, 0, 0, 0),
        Arguments.of("9.0.3", 9, 0, 3, 0),
        Arguments.of("11", 11, 0, 0, 0),

        // malformed version
        Arguments.of("invalid version", -1, -1, -1, -1)
    );
  }

  @ParameterizedTest
  @MethodSource("provideVersions")
  public void testParseVersion(String rawVersionString, int expectedMajorVersion, int expectedMinorVersion,
        int expectedMaintenanceVersion, int expectedUpdateVersion) {
    JdkVersion version = JdkVersion.parseVersion(rawVersionString);

    assertEquals(expectedMajorVersion, version.getMajor());
    assertEquals(expectedMinorVersion, version.getMinor());
    assertEquals(expectedMaintenanceVersion, version.getMaintenance());
    assertEquals(expectedUpdateVersion, version.getUpdate());
  }
}
