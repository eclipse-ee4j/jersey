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

import jakarta.ws.rs.client.WebTarget;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for monitoring statistics injection.
 *
 * @author Jakub Podlesak
 */
public class MonitoringTest extends CdiTest {

    public static Stream<String> testData() {
        return Stream.of("app-field-injected", "app-ctor-injected", "request-field-injected", "request-ctor-injected");
    }

    /**
     * Make several requests and check the counter keeps incrementing.
     *
     * @throws Exception in case of unexpected test failure.
     */
    @ParameterizedTest
    @MethodSource("testData")
    public void testRequestCount(String resource) throws Exception {
        final WebTarget target = target().path(resource).path("requestCount");
        Thread.sleep(1000); // this is to allow statistics on the server side to get updated
        final int start = Integer.decode(target.request().get(String.class));
        for (int i = 1; i < 4; i++) {
            Thread.sleep(1000); // this is to allow statistics on the server side to get updated
            final int next = Integer.decode(target.request().get(String.class));
            assertThat(String.format("testing %s", resource), next, equalTo(start + i));
        }
    }
}
