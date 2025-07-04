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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomConnectionControllerTest extends JerseyTest {
    final AtomicBoolean hit = new AtomicBoolean(false);

    @Path("/")
    public static class CustomConnectionControllerTestResource {
        @GET
        public String get() {
            return "ok";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CustomConnectionControllerTestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        NettyConnectorProvider provider = NettyConnectorProvider.config().connectionController(new NettyConnectionController() {
            @Override
            public String getConnectionGroup(ClientRequest clientRequest, URI uri, String hostName, int port) {
                hit.set(true);
                return super.getConnectionGroup(clientRequest, uri, hostName, port);
            }
        }).build();

        config.connectorProvider(provider);
    }

    @Test
    public void testCustomConnectionControllerIsInvoked() {
        try (Response response = target().request().get()) {
            Assertions.assertEquals(200, response.getStatus());
        }
        client().close();
        Assertions.assertEquals(true, hit.get());

    }
}
