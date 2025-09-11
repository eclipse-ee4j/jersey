/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the connector provider correctly handles redirects.
 */
public class RedirectTest extends AbstractJavaConnectorTest {
    /**
     * Checks, that without further configuration redirects are taken.
     */
    @Test
    public void testRedirect() {
        assertThat(this.request("java-connector/redirect").readEntity(String.class)).isEqualTo("Hello World!");
    }

    /**
     * Checks, that no redirect happens, if the redirects are switched off.
     */
    @Test
    public void testNotFollowRedirects() {
        Response response = target().path("java-connector").path("redirect")
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
    }
}
