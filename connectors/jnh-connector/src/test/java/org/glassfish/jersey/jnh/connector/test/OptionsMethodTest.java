/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks, that an {@code OPTIONS} request may be sent to a {@code GET} endpoint.
 */
public class OptionsMethodTest extends AbstractJavaConnectorTest {
    /**
     * Sends an {@code OPTIONS} request to the root {@code GET} endpoint and assumes a code 200.
     */
    @Test
    public void testOptionsMethod() {
        assertThat(this.requestWithEntity("java-connector", "OPTIONS", null).getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
    }
}
