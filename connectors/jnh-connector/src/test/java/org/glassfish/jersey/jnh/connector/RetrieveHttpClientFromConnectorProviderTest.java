/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector;

import org.junit.Test;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests access to the {@link HttpClient} instance provided from the {@link JavaNetHttpConnectorProvider}.
 */
public class RetrieveHttpClientFromConnectorProviderTest extends AbstractJavaConnectorTest {
    /**
     * Checks, that the {@link jakarta.ws.rs.client.Client} and {@link jakarta.ws.rs.client.WebTarget} instances
     * correctly return the internally used {@link HttpClient}.
     */
    @Test
    public void testClientUsesJavaConnector() {
        assertThat(JavaNetHttpConnectorProvider.getHttpClient(client())).isInstanceOf(HttpClient.class);
        assertThat(JavaNetHttpConnectorProvider.getHttpClient(target())).isInstanceOf(HttpClient.class);
        assertThat(JavaNetHttpConnectorProvider.getHttpClient(client()))
                .isEqualTo(JavaNetHttpConnectorProvider.getHttpClient(target()));
    }
}
