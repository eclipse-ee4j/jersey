/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.agnostic;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JettyHeaderTest extends JerseyTest {
    @Path("/")
    public static class JettyHeaderTestResource {
        @Context
        HttpHeaders httpHeaders;

        @GET
        public String get() {
            return httpHeaders.getHeaderString(HttpHeaders.USER_AGENT);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(JettyHeaderTestResource.class);
                //.register(LoggingFeature.builder().level(Level.INFO).verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY).build());
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JettyConnectorProvider());
    }

    @Test
    public void helloWorldTest() {
        try (Response r = target().request().get()) {
            Assertions.assertEquals(200, r.getStatus());

            int index = -1;
            String userAgent = r.readEntity(String.class);
            index = userAgent.indexOf("Jersey");
            Assertions.assertTrue(index != -1);

            index = userAgent.indexOf("Jersey", index + 1);
            Assertions.assertEquals(-1, index);
        }
    }
}
