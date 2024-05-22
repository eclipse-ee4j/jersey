/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.jetty.JettyTestContainerProperties;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HugeHeaderTest extends JerseyTest {

    private static final int SERVER_HEADER_SIZE = 1234567;

    private static final String hugeHeader =
              "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz";

    @Path("/test")
    public static class HttpMethodResource {
        @POST
        public Response post(
                @HeaderParam("X-HUGE-HEADER") String hugeHeader,
                String entity) {

            return Response.noContent()
                    .header("X-HUGE-HEADER", hugeHeader)
                    .header("X-HUGE-HEADER-SIZE", hugeHeader.length())
                    .build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HugeHeaderTest.HttpMethodResource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        final Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put(JettyTestContainerProperties.HEADER_SIZE, SERVER_HEADER_SIZE);
        return new JettyTestContainerFactory(configurationProperties);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testContentHeaderTrunked() {
        final StringBuffer veryHugeHeader = new StringBuffer();
        for (int i = 1; i < 33; i++) {
            veryHugeHeader.append(hugeHeader);
        }
        final Response response = target("test").request()
                .header("X-HUGE-HEADER", veryHugeHeader.toString())
                .post(null);

        assertNull(response.getHeaderString("X-HUGE-HEADER-SIZE"));
        assertNull(response.getHeaderString("X-HUGE-HEADER"));
        response.close();
    }

    @Test
    public void testConnectorHeaderSize() {
        final StringBuffer veryHugeHeader = new StringBuffer();
        for (int i = 1; i < 35; i++) {
            veryHugeHeader.append(hugeHeader);
        }
        int headerSize = veryHugeHeader.length();
        Response response = target("test")
                .property(NettyClientProperties.MAX_HEADER_SIZE, 77750)
                        .request()


                        .header("X-HUGE-HEADER", veryHugeHeader.toString())
                        .post(null);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        assertEquals(String.valueOf(headerSize), response.getHeaderString("X-HUGE-HEADER-SIZE"));
        assertEquals(veryHugeHeader.toString(), response.getHeaderString("X-HUGE-HEADER"));
        response.close();
    }
}
