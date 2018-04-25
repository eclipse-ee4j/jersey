/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.inmemory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

/**
 * In-memory client connector.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class InMemoryConnector implements Connector {

    private static final Logger LOGGER = Logger.getLogger(InMemoryConnector.class.getName());

    private final URI baseUri;
    private final ApplicationHandler appHandler;

    /**
     * In-memory client connector provider.
     */
    static class Provider implements ConnectorProvider {

        private final URI baseUri;
        private final ApplicationHandler appHandler;

        /**
         * Create new in-memory connector provider.
         *
         * @param baseUri    application base URI.
         * @param appHandler RequestInvoker instance which represents application.
         */
        Provider(URI baseUri, ApplicationHandler appHandler) {
            this.baseUri = baseUri;
            this.appHandler = appHandler;
        }

        @Override
        public Connector getConnector(Client client, Configuration config) {
            return new InMemoryConnector(baseUri, appHandler);
        }
    }

    /**
     * Create new in-memory connector.
     *
     * @param baseUri    application base URI.
     * @param appHandler RequestInvoker instance which represents application.
     */
    private InMemoryConnector(final URI baseUri, final ApplicationHandler appHandler) {
        this.baseUri = baseUri;
        this.appHandler = appHandler;
    }

    /**
     * In memory container response writer.
     */
    public static class InMemoryResponseWriter implements ContainerResponseWriter {

        private MultivaluedMap<String, String> headers;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private boolean committed;
        private Response.StatusType statusInfo;

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) {
            List<Object> length = new ArrayList<>();
            length.add(String.valueOf(contentLength));

            responseContext.getHeaders().put(HttpHeaders.CONTENT_LENGTH, length);
            headers = responseContext.getStringHeaders();
            statusInfo = responseContext.getStatusInfo();
            return baos;
        }

        @Override
        public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
            LOGGER.warning("Asynchronous server side invocations are not supported by InMemoryContainer.");
            return false;
        }

        @Override
        public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) {
            throw new UnsupportedOperationException("Async server side invocations are not supported by InMemoryContainer.");
        }

        @Override
        public void commit() {
            committed = true;
        }

        @Override
        public void failure(Throwable error) {
            throw new ProcessingException("Server-side request processing failed with an error.", error);
        }

        @Override
        public boolean enableResponseBuffering() {
            return true;
        }

        /**
         * Get the written entity.
         *
         * @return Byte array which contains the entity written by the server.
         */
        public byte[] getEntity() {
            if (!committed) {
                throw new IllegalStateException("Response is not committed yet.");
            }
            return baos.toByteArray();
        }

        /**
         * Return response headers.
         *
         * @return headers.
         */
        public MultivaluedMap<String, String> getHeaders() {
            return headers;
        }

        /**
         * Returns response status info.
         *
         * @return status info.
         */
        public Response.StatusType getStatusInfo() {
            return statusInfo;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Transforms client-side request to server-side and invokes it on provided application ({@link ApplicationHandler}
     * instance).
     *
     * @param clientRequest client side request to be invoked.
     */
    @Override
    public ClientResponse apply(final ClientRequest clientRequest) {
        PropertiesDelegate propertiesDelegate = new MapPropertiesDelegate();

        final ContainerRequest containerRequest = new ContainerRequest(baseUri,
                clientRequest.getUri(), clientRequest.getMethod(),
                null, propertiesDelegate);

        containerRequest.getHeaders().putAll(clientRequest.getStringHeaders());

        final ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
        if (clientRequest.getEntity() != null) {
            clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                @Override
                public OutputStream getOutputStream(int contentLength) throws IOException {
                    final MultivaluedMap<String, Object> clientHeaders = clientRequest.getHeaders();
                    if (contentLength != -1 && !clientHeaders.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                        containerRequest.getHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
                    }
                    return clientOutput;
                }
            });
            clientRequest.enableBuffering();

            try {
                clientRequest.writeEntity();
            } catch (IOException e) {
                final String msg = "Error while writing entity to the output stream.";
                LOGGER.log(Level.SEVERE, msg, e);
                throw new ProcessingException(msg, e);
            }
        }

        containerRequest.setEntityStream(new ByteArrayInputStream(clientOutput.toByteArray()));

        boolean followRedirects = ClientProperties.getValue(clientRequest.getConfiguration().getProperties(),
                ClientProperties.FOLLOW_REDIRECTS, true);

        final InMemoryResponseWriter inMemoryResponseWriter = new InMemoryResponseWriter();
        containerRequest.setWriter(inMemoryResponseWriter);
        containerRequest.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        });
        appHandler.handle(containerRequest);

        return tryFollowRedirects(followRedirects,
                createClientResponse(
                        clientRequest,
                        inMemoryResponseWriter),
                new ClientRequest(clientRequest));

    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        CompletableFuture<ClientResponse> future = new CompletableFuture<>();
        try {
            ClientResponse response = apply(request);
            callback.response(response);
            future.complete(response);
        } catch (ProcessingException ex) {
            future.completeExceptionally(ex);
        } catch (Throwable t) {
            callback.failure(t);
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public void close() {
        // do nothing
    }

    private ClientResponse createClientResponse(final ClientRequest clientRequest,
                                                final InMemoryResponseWriter responseWriter) {
        final ClientResponse clientResponse = new ClientResponse(responseWriter.getStatusInfo(), clientRequest);
        clientResponse.getHeaders().putAll(responseWriter.getHeaders());
        clientResponse.setEntityStream(new ByteArrayInputStream(responseWriter.getEntity()));
        return clientResponse;
    }

    @SuppressWarnings("MagicNumber")
    private ClientResponse tryFollowRedirects(boolean followRedirects, ClientResponse response, ClientRequest request) {
        if (!followRedirects) {
            return response;
        }

        while (true) {
            switch (response.getStatus()) {
                case 303:
                case 302:
                case 307:
                    request = new ClientRequest(request);
                    request.setUri(response.getLocation());
                    if (response.getStatus() == 303) {
                        request.setMethod("GET");
                    }
                    response = apply(request);
                    break;
                default:
                    return response;
            }
        }
    }

    @Override
    public String getName() {
        return "Jersey InMemory Connector";
    }
}
