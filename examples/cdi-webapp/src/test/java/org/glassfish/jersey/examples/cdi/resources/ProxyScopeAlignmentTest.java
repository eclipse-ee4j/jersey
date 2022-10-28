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
 * Ensure CDI and JAX-RS scopes are well aligned, so that dynamic proxies
 * are only created when needed.
 *
 * @author Jakub Podlesak
 */
public class ProxyScopeAlignmentTest extends CdiTest {

    public static Stream<String> testData() {
        return Stream.of("one", "too", "much");
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testUiInjection(String p) {

        final WebTarget app = target().path("ui-app").path(p);
        final WebTarget req = target().path("ui-req").path(p);

        String ar = app.request().get(String.class);
        String rr = req.request().get(String.class);

        assertThat(ar, containsString(p));
        assertThat(rr, containsString(p));
    }
}
