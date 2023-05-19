/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2421;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.OutboundMessageContext;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Reproducer tests for JERSEY-2421.
 *
 * @author Michal Gajdos
 */
public class Jersey2421Test {

    private static class TestConnector implements Connector, ConnectorProvider {

        @Override
        public ClientResponse apply(final ClientRequest request) {
            try {
                request.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(final int contentLength) throws IOException {
                        return OutputStream.nullOutputStream();
                    }
                });
                request.writeEntity();

                if (request.getHeaderString("Content-Type").contains("boundary")) {
                    return new ClientResponse(Response.Status.OK, request);
                }
            } catch (final IOException ioe) {
                // NOOP
            }
            return new ClientResponse(Response.Status.BAD_REQUEST, request);
        }

        @Override
        public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public Connector getConnector(final Client client, final Configuration runtimeConfig) {
            return this;
        }
    }

    /**
     * Test that multipart feature works on the client-side - custom connector checks presence of {@code boundary} parameter in
     * the {@code Content-Type} header (the header is added to the request in MBW).
     */
    @Test
    public void testMultiPartFeatureOnClient() throws Exception {
        final Client client = ClientBuilder.newClient(new ClientConfig().connectorProvider(new TestConnector()))
                .register(MultiPartFeature.class);

        final MultiPart entity = new FormDataMultiPart()
                .bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("part").build(), "CONTENT"));

        final Response response = client.target("http://localhost").request()
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

        assertThat(response.getStatus(), is(200));
    }

    /**
     * Test that classes from jersey-server module cannot be loaded.
     */
    @Test
    public void testLoadJerseyServerClass() throws Exception {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.glassfish.jersey.server.ResourceConfig"));
    }
}
