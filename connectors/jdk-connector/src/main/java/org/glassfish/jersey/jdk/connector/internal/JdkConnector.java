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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class JdkConnector implements Connector {

    private final HttpConnectionPool httpConnectionPool;
    private final ConnectorConfiguration connectorConfiguration;

    public JdkConnector(Client client, Configuration config) {
        connectorConfiguration = new ConnectorConfiguration(client, config);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(connectorConfiguration.getCookiePolicy());
        httpConnectionPool = new HttpConnectionPool(connectorConfiguration, cookieManager);
    }

    @Override
    public ClientResponse apply(ClientRequest request) {

        Future<?> future = apply(request, new AsyncConnectorCallback() {
            @Override
            public void response(ClientResponse response) {

            }

            @Override
            public void failure(Throwable failure) {

            }
        });

        try {
            return (ClientResponse) future.get();
        } catch (Exception e) {
            throw new ProcessingException(unwrapExecutionException(e));
        }
    }

    private Throwable unwrapExecutionException(Throwable failure) {
        return (failure != null && failure instanceof ExecutionException) ? failure.getCause() : failure;
    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        final CompletableFuture<ClientResponse> responseFuture = new CompletableFuture<>();
        // just so we don't have to drag around both the future and callback
        final AsyncConnectorCallback internalCallback = new AsyncConnectorCallback() {
            @Override
            public void response(ClientResponse response) {
                callback.response(response);
                responseFuture.complete(response);
            }

            @Override
            public void failure(Throwable failure) {
                Throwable actualFailure = unwrapExecutionException(failure);
                callback.failure(actualFailure);
                responseFuture.completeExceptionally(actualFailure);
            }
        };

        final HttpRequest httpRequest = createHttpRequest(request);

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.BUFFERED) {
            writeBufferedEntity(request, httpRequest, internalCallback);
        }

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.BUFFERED
                || httpRequest.getBodyMode() == HttpRequest.BodyMode.NONE) {
            send(request, httpRequest, internalCallback);
        }

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.CHUNKED) {

            /* We wait with sending the request header until the body stream has been touched.
             This is because of javax.ws.rs.ext.MessageBodyWriter, which says:

             "The message header map is mutable but any changes must be made before writing to the output stream since
              the headers will be flushed prior to writing the message body"

              This means that the headers can change until body output stream is used.
              */
            final InterceptingOutputStream bodyStream = new InterceptingOutputStream(httpRequest.getBodyStream(),
                    // send the prepared request when the stream is touched for the first time
                    () -> send(request, httpRequest, internalCallback));

            request.setStreamProvider(contentLength -> bodyStream);
            try {
                request.writeEntity();
            } catch (IOException e) {
                internalCallback.failure(e);
            }
        }

        return responseFuture;
    }

    private void writeBufferedEntity(ClientRequest request, final HttpRequest httpRequest, AsyncConnectorCallback callback) {
        request.setStreamProvider(contentLength -> httpRequest.getBodyStream());
        try {
            request.writeEntity();
        } catch (IOException e) {
            callback.failure(e);
        }
    }

    private void send(final ClientRequest request, final HttpRequest httpRequest, final AsyncConnectorCallback callback) {
        translateHeaders(request, httpRequest);
        final RedirectHandler redirectHandler = new RedirectHandler(httpConnectionPool, httpRequest, connectorConfiguration);
        httpConnectionPool.send(httpRequest, new CompletionHandler<HttpResponse>() {

            @Override
            public void failed(Throwable throwable) {
                callback.failure(throwable);
            }

            @Override
            public void completed(HttpResponse result) {
                redirectHandler.handleRedirects(result, new CompletionHandler<HttpResponse>() {
                    @Override
                    public void failed(Throwable throwable) {
                        Throwable actualFailure = unwrapExecutionException(throwable);
                        callback.failure(actualFailure);
                    }

                    @Override
                    public void completed(HttpResponse result) {
                        ClientResponse response = translateResponse(request, result, redirectHandler.getLastRequestUri());
                        callback.response(response);
                    }
                });
            }
        });
    }

    private HttpRequest createHttpRequest(ClientRequest request) {
        Object entity = request.getEntity();

        if (entity == null) {
            return HttpRequest.createBodyless(request.getMethod(), request.getUri());
        }

        RequestEntityProcessing entityProcessing = request.resolveProperty(
                ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.class);

        HttpRequest httpRequest;
        if (entityProcessing != null && entityProcessing == RequestEntityProcessing.CHUNKED) {
            httpRequest = HttpRequest.createChunked(request.getMethod(), request.getUri(), connectorConfiguration.getChunkSize());
        } else {
            httpRequest = HttpRequest.createBuffered(request.getMethod(), request.getUri());
        }

        return httpRequest;
    }

    private Map<String, List<String>> translateHeaders(ClientRequest clientRequest, HttpRequest httpRequest) {
        Map<String, List<String>> headers = httpRequest.getHeaders();
        for (Map.Entry<String, List<String>> header : clientRequest.getStringHeaders().entrySet()) {
            List<String> values = new ArrayList<>(header.getValue());
            headers.put(header.getKey(), values);
        }

        return headers;
    }

    private ClientResponse translateResponse(final ClientRequest requestContext,
                                             final HttpResponse httpResponse,
                                             URI requestUri) {

        Response.StatusType statusType = new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return httpResponse.getStatusCode();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.familyOf(httpResponse.getStatusCode());
            }

            @Override
            public String getReasonPhrase() {
                return httpResponse.getReasonPhrase();
            }
        };

        ClientResponse responseContext = new ClientResponse(statusType, requestContext, requestUri);

        Map<String, List<String>> headers = httpResponse.getHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                responseContext.getHeaders().add(entry.getKey(), value);
            }
        }

        responseContext.setEntityStream(httpResponse.getBodyStream());
        return responseContext;
    }

    @Override
    public String getName() {
        return "JDK connector";
    }

    @Override
    public void close() {
        httpConnectionPool.close();
    }
}
