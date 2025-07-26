/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.integration.helidon;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.helidon.connector.HelidonConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Helidon3Test extends JerseyTest {

    @Path("/")
    public static class Helidon3TestResource {
        @POST
        @Path("version")
        public String header(@Context HttpHeaders headers, String content) {
            return headers.getHeaderString(HttpHeaders.USER_AGENT);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Helidon3TestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new HelidonConnectorProvider());
        super.configureClient(config);
    }

    @Test
    public void testPostWithHelidon3() {
        System.out.println("Helidon Version " + io.helidon.common.Version.VERSION);
        Assertions.assertEquals('3', io.helidon.common.Version.VERSION.charAt(0));
        try (Response response = target("version").request().post(Entity.entity("ANYTHING", MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertTrue(response.readEntity(String.class).contains("Helidon/3"));
        }
    }
}
