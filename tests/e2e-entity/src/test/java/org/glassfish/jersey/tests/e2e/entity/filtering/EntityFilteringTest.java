/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity.filtering;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.test.JerseyTest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Common parent class for Entity Filtering tests.
 *
 * @author Michal Gajdos
 */
abstract class EntityFilteringTest extends JerseyTest {

    /**
     * Test that (filtered) fields of given strings are equal (fields are separated by commas).
     *
     * @param actual actual fields from response received from server.
     * @param expected expected fields.
     */
    static void assertSameFields(final String actual, final String expected) {
        final Set<String> actualSet = Arrays.stream(actual.split(",")).collect(Collectors.toSet());
        final Set<String> expectedSet = Arrays.stream(expected.split(",")).collect(Collectors.toSet());

        assertThat(actualSet, equalTo(expectedSet));
    }
}
