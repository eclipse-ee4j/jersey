/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Reproduces issue #6040: when the request has a body and chunked transfer encoding is not used,
 * no {@code LastHttpContent} is written to the channel. The encoder stays in the
 * {@code ST_CONTENT_NON_CHUNK} state, and the next request sent on the pooled channel fails.
 */
public class NonChunkedEntityConnectionReuseTest extends JerseyTest {

    private static final int REQUEST_COUNT = 4;
    private static final byte[] ENTITY = new byte[64 * 1024];

    @Path("/upload")
    public static class UploadResource {
        @PUT
        @Path("{name}")
        public Response put(@PathParam("name") String name, byte[] entity) {
            return Response.ok(String.valueOf(entity.length)).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(UploadResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testContentLengthRequestsOnOneChannel() {
        for (int i = 1; i <= REQUEST_COUNT; i++) {
            try (Response response = target("upload").path("entity-" + i).request()
                    .header(HttpHeaders.CONTENT_LENGTH, ENTITY.length)
                    .put(Entity.entity(ENTITY, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {
                Assertions.assertEquals(200, response.getStatus(), "request " + i + " failed");
                Assertions.assertEquals(String.valueOf(ENTITY.length), response.readEntity(String.class),
                        "request " + i + " did not reach the resource with the whole entity");
            }
        }
    }

    @Test
    public void testBufferedRequestsOnOneChannel() {
        ClientConfig config = new ClientConfig()
                .connectorProvider(new NettyConnectorProvider())
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
        Client client = ClientBuilder.newClient(config);
        try {
            for (int i = 1; i <= REQUEST_COUNT; i++) {
                try (Response response = client.target(getBaseUri()).path("upload").path("buffered-" + i).request()
                        .put(Entity.entity(ENTITY, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {
                    Assertions.assertEquals(200, response.getStatus(), "request " + i + " failed");
                    Assertions.assertEquals(String.valueOf(ENTITY.length), response.readEntity(String.class),
                            "request " + i + " did not reach the resource with the whole entity");
                }
            }
        } finally {
            client.close();
        }
    }
}
