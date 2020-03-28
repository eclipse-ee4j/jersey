/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jettyresponseclose;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;

public class JettyHttpContainerCloseTest {

    private static Server server;
    private static JettyHttpContainer container;
    private static final String URL = "http://localhost:9080";

    @BeforeAll
    public static void setup() {
        server = JettyHttpContainerFactory.createServer(URI.create(URL),
                new ResourceConfig(Resource.class));
        container = (JettyHttpContainer) server.getHandler();
    }

    @AfterAll
    public static void teardown() throws Exception {
        container.doStop();
    }

    @Test
    public void testResponseClose() {
        try (Response response = ClientBuilder.newClient().target(URL).request().get()) {
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals(Resource.class.getName(), response.readEntity(String.class));

        }
    }
}
