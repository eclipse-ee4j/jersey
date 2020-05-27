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

package org.glassfish.jersey.helidon.connector;

import io.helidon.common.Version;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientRequestBuilder;
import io.helidon.webclient.WebClientResponse;
import org.glassfish.jersey.client.ClientAsyncExecutorLiteral;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * A {@link Connector} that utilizes the Helidon HTTP Client to send and receive
 * HTTP request and responses.
 */
class HelidonConnector implements Connector {

    private static final String helidonVersion = "Helidon/" + Version.VERSION + " (java " + AccessController
            .doPrivileged(PropertiesHelper.getSystemProperty("java.runtime.version")) + ")";
    static final Logger LOGGER = Logger.getLogger(HelidonConnector.class.getName());

    private final WebClient webClient;

    private final ExecutorServiceKeeper executorServiceKeeper;
    private final HelidonEntity.HelidonEntityType entityType;

    private static final InputStream NO_CONTENT_INPUT_STREAM = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    // internal implementation entity type, can be removed in the future
    // settable for testing purposes
    private static final String INTERNAL_ENTITY_TYPE = "jersey.config.helidon.client.entity.type";

    HelidonConnector(final Client client, final Configuration config) {
        executorServiceKeeper = new ExecutorServiceKeeper(client);
        entityType = getEntityType(config);

        final WebClient.Builder webClientBuilder = WebClient.builder();

        webClientBuilder.addReader(HelidonStructures.createInputStreamBodyReader());
        HelidonEntity.helidonWriter(entityType).ifPresent(webClientBuilder::addWriter);

        HelidonStructures.createProxy(config).ifPresent(webClientBuilder::proxy);

        HelidonStructures.helidonConfig(config).ifPresent(webClientBuilder::config);

        webClientBuilder.connectTimeout(ClientProperties.getValue(config.getProperties(),
                ClientProperties.CONNECT_TIMEOUT, 10000), ChronoUnit.MILLIS);

        HelidonStructures.createSSL(client.getSslContext()).ifPresent(webClientBuilder::ssl);

        webClient = webClientBuilder.build();
    }

    @Override
    public ClientResponse apply(ClientRequest request) {
        try {
            return applyInternal(request).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ProcessingException(e);
        }
    }

    @Override
    public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
        final BiConsumer<? super ClientResponse, ? super Throwable> action = (r, th) -> {
            if (th == null) callback.response(r);
            else callback.failure(th);
        };
        return applyInternal(request)
                .whenCompleteAsync(action, executorServiceKeeper.getExecutorService(request))
                .toCompletableFuture();
    }

    @Override
    public String getName() {
        return helidonVersion;
    }

    @Override
    public void close() {

    }

    private CompletionStage<ClientResponse> applyInternal(ClientRequest request) {
        final WebClientRequestBuilder webClientRequestBuilder = webClient.method(request.getMethod());
        webClientRequestBuilder.uri(request.getUri());

        webClientRequestBuilder.headers(HelidonStructures.createHeaders(request.getRequestHeaders()));

        for (String propertyName : request.getConfiguration().getPropertyNames()) {
            Object property = request.getConfiguration().getProperty(propertyName);
            if (!propertyName.startsWith("jersey") && String.class.isInstance(property)) {
                webClientRequestBuilder.property(propertyName, (String) property);
            }
        }

        for (String propertyName : request.getPropertyNames()) {
            Object property = request.resolveProperty(propertyName, null);
            if (!propertyName.startsWith("jersey") && String.class.isInstance(property)) {
                webClientRequestBuilder.property(propertyName, (String) property);
            }
        }

        // 2.0.0-M3
        // HelidonStructures.createProxy(request).ifPresent(webClientRequestBuilder::proxy);

        webClientRequestBuilder.followRedirects(request.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, true));
        webClientRequestBuilder.readTimeout(request.resolveProperty(ClientProperties.READ_TIMEOUT, 10000), ChronoUnit.MILLIS);

        CompletionStage<WebClientResponse> responseStage = null;

        if (request.hasEntity()) {
            responseStage = HelidonEntity.submit(
                    entityType, request, webClientRequestBuilder, executorServiceKeeper.getExecutorService(request)
            );
        } else {
            responseStage = webClientRequestBuilder.submit();
        }

        return responseStage.thenCompose((a) -> convertResponse(request, a));
    }

    private CompletionStage<ClientResponse> convertResponse(final ClientRequest requestContext,
                                                            final WebClientResponse webClientResponse) {

        final ClientResponse responseContext = new ClientResponse(new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return webClientResponse.status().code();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.familyOf(getStatusCode());
            }

            @Override
            public String getReasonPhrase() {
                return webClientResponse.status().reasonPhrase();
            }
        }, requestContext);

        for (Map.Entry<String, List<String>> entry : webClientResponse.headers().toMap().entrySet()) {
            for (String value : entry.getValue()) {
                responseContext.getHeaders().add(entry.getKey(), value);
            }
        }

        responseContext.setResolvedRequestUri(webClientResponse.lastEndpointURI());

        final CompletionStage<InputStream> stream = HelidonStructures.hasEntity(webClientResponse)
                ? webClientResponse.content().as(InputStream.class)
                : CompletableFuture.supplyAsync(() -> NO_CONTENT_INPUT_STREAM);

        return stream.thenApply((a) -> {
            responseContext.setEntityStream(new FilterInputStream(a) {
                private final AtomicBoolean closed = new AtomicBoolean(false);

                @Override
                public void close() throws IOException {
                    // Avoid idempotent close in the underlying input stream
                    if (!closed.compareAndSet(false, true)) {
                        super.close();
                    }
                }
            });
            return responseContext;
        });
    }

    private static HelidonEntity.HelidonEntityType getEntityType(final Configuration config) {
        final String helidonType = ClientProperties.getValue(config.getProperties(),
                INTERNAL_ENTITY_TYPE, HelidonEntity.HelidonEntityType.READABLE_BYTE_CHANNEL.name());
        final HelidonEntity.HelidonEntityType entityType = HelidonEntity.HelidonEntityType.valueOf(helidonType);

//        if (entityType != HelidonEntity.HelidonEntityType.READABLE_BYTE_CHANNEL) {
//            // log warning for internal feature - no localization.properties
//            LOGGER.warning(INTERNAL_ENTITY_TYPE + " is " + entityType.name());
//        }

        return entityType;
    }

    private static class ExecutorServiceKeeper {
        private Optional<ExecutorService> executorService;

        private ExecutorServiceKeeper(Client client) {
            final ClientConfig config = ((JerseyClient) client).getConfiguration();
            executorService = Optional.ofNullable(config.getExecutorService());
        }

        private ExecutorService getExecutorService(ClientRequest request) {
            if (!executorService.isPresent()) {
                // cache for multiple requests
                executorService = Optional.ofNullable(request.getInjectionManager()
                        .getInstance(ExecutorServiceProvider.class, ClientAsyncExecutorLiteral.INSTANCE).getExecutorService());
            }

            return executorService.get();
        }
    }
}
