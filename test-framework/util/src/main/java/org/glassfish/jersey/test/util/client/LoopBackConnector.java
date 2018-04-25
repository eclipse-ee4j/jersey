/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.util.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.OutboundMessageContext;

/**
 * Loop-Back connector used for testing/benchmarking purposes. It returns a response that contains the same data (headers, entity)
 * as the processed request. The status of the response is {@code 600}.
 *
 * @author Michal Gajdos
 * @since 2.17
 */
final class LoopBackConnector implements Connector {

    /**
     * Test loop-back status code.
     */
    static final int TEST_LOOPBACK_CODE = 600;

    /**
     * Test loop-back status type.
     */
    static final Response.StatusType LOOPBACK_STATUS = new Response.StatusType() {
        @Override
        public int getStatusCode() {
            return TEST_LOOPBACK_CODE;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.OTHER;
        }

        @Override
        public String getReasonPhrase() {
            return "Test connector loop-back";
        }
    };

    private volatile boolean closed = false;

    @Override
    public ClientResponse apply(final ClientRequest request) {
        return _apply(request);
    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        CompletableFuture<ClientResponse> future = new CompletableFuture<>();
        try {
            ClientResponse response = _apply(request);
            callback.response(response);
            future.complete(response);
        } catch (final Throwable t) {
            callback.failure(t);
            future.completeExceptionally(t);
        }
        return future;
    }

    private ClientResponse _apply(final ClientRequest request) {
        checkNotClosed();
        final ClientResponse response = new ClientResponse(LOOPBACK_STATUS, request);

        // Headers.
        response.headers(HeaderUtils.asStringHeaders(request.getHeaders()));

        // Entity.
        if (request.hasEntity()) {
            response.setEntityStream(new ByteArrayInputStream(bufferEntity(request)));
        }

        return response;
    }

    private byte[] bufferEntity(final ClientRequest requestContext) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);

        requestContext.setStreamProvider(new OutboundMessageContext.StreamProvider() {

            @Override
            public OutputStream getOutputStream(final int contentLength) throws IOException {
                return baos;
            }
        });

        try {
            requestContext.writeEntity();
        } catch (final IOException ioe) {
            throw new ProcessingException("Error buffering the entity.", ioe);
        }

        return baos.toByteArray();
    }

    @Override
    public String getName() {
        return "test-loop-back-connector";
    }

    @Override
    public void close() {
        closed = true;
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Loop-back Connector closed.");
        }
    }
}
