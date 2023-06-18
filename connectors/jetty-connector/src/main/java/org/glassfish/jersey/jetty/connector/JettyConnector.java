/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieStore;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MultivaluedMap;

import javax.net.ssl.SSLContext;

import org.eclipse.jetty.client.AuthenticationStore;
import org.eclipse.jetty.client.BasicAuthentication;
import org.eclipse.jetty.client.ByteBufferRequestContent;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.FutureResponseListener;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.OutputStreamRequestContent;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.transport.HttpRequest;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.io.ClientConnector;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.util.collection.ByteBufferInputStream;
import org.glassfish.jersey.internal.util.collection.NonBlockingInputStream;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.Statuses;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * A {@link Connector} that utilizes the Jetty HTTP Client to send and receive
 * HTTP request and responses.
 * <p/>
 * The following properties are only supported at construction of this class:
 * <ul>
 * <li>{@link ClientProperties#ASYNC_THREADPOOL_SIZE}</li>
 * <li>{@link ClientProperties#CONNECT_TIMEOUT}</li>
 * <li>{@link ClientProperties#FOLLOW_REDIRECTS}</li>
 * <li>{@link ClientProperties#PROXY_URI}</li>
 * <li>{@link ClientProperties#PROXY_USERNAME}</li>
 * <li>{@link ClientProperties#PROXY_PASSWORD}</li>
 * <li>{@link ClientProperties#PROXY_PASSWORD}</li>
 * <li>{@link JettyClientProperties#DISABLE_COOKIES}</li>*
 * <li>{@link JettyClientProperties#ENABLE_SSL_HOSTNAME_VERIFICATION}</li>
 * <li>{@link JettyClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION}</li>
 * <li>{@link JettyClientProperties#SYNC_LISTENER_RESPONSE_MAX_SIZE}</li>
 * </ul>
 * <p/>
 * This transport supports both synchronous and asynchronous processing of client requests.
 * The following methods are supported: GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT and MOVE.
 * <p/>
 * Typical usage:
 * <p/>
 * <pre>
 * {@code
 * ClientConfig config = new ClientConfig();
 * Connector connector = new JettyConnector(config);
 * config.connector(connector);
 * Client client = ClientBuilder.newClient(config);
 *
 * // async request
 * WebTarget target = client.target("http://localhost:8080");
 * Future<Response> future = target.path("resource").request().async().get();
 *
 * // wait for 3 seconds
 * Response response = future.get(3, TimeUnit.SECONDS);
 * String entity = response.readEntity(String.class);
 * client.close();
 * }
 * </pre>
 * <p>
 * This connector supports only {@link org.glassfish.jersey.client.RequestEntityProcessing#BUFFERED entity buffering}.
 * Defining the property {@link ClientProperties#REQUEST_ENTITY_PROCESSING} has no effect on this connector.
 * </p>
 *
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Marek Potociar
 */
class JettyConnector implements Connector {

    private static final Logger LOGGER = Logger.getLogger(JettyConnector.class.getName());

    private final HttpClient client;
    private final HttpCookieStore cookieStore;
    private final Configuration configuration;
    private final Optional<Integer> syncListenerResponseMaxSize;

    /**
     * Create the new Jetty client connector.
     *
     * @param jaxrsClient JAX-RS client instance, for which the connector is created.
     * @param config client configuration.
     */
    JettyConnector(final Client jaxrsClient, final Configuration config) {
        this.configuration = config;
        HttpClient httpClient = null;
        if (config.isRegistered(JettyHttpClientSupplier.class)) {
            Optional<Object> contract = config.getInstances().stream()
                    .filter(a-> JettyHttpClientSupplier.class.isInstance(a)).findFirst();
            if (contract.isPresent()) {
                httpClient = ((JettyHttpClientSupplier) contract.get()).getHttpClient();
            }
        }
        if (httpClient == null) {
            final SSLContext sslContext = jaxrsClient.getSslContext();
            final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(false);
            sslContextFactory.setSslContext(sslContext);
            final ClientConnector connector = new ClientConnector();
            connector.setSslContextFactory(sslContextFactory);
            final HttpClientTransport transport = new HttpClientTransportOverHTTP(connector);
            httpClient = new HttpClient(transport);
        }
        this.client = httpClient;

        Boolean enableHostnameVerification = (Boolean) config.getProperties()
                .get(JettyClientProperties.ENABLE_SSL_HOSTNAME_VERIFICATION);
        if (enableHostnameVerification != null) {
            final String verificationAlgorithm = enableHostnameVerification ? "HTTPS" : null;
            client.getSslContextFactory().setEndpointIdentificationAlgorithm(verificationAlgorithm);
        }
        if (jaxrsClient.getHostnameVerifier() != null) {
            client.getSslContextFactory().setHostnameVerifier(jaxrsClient.getHostnameVerifier());
        }

        final Object connectTimeout = config.getProperties().get(ClientProperties.CONNECT_TIMEOUT);
        if (connectTimeout != null && connectTimeout instanceof Integer && (Integer) connectTimeout > 0) {
            client.setConnectTimeout((Integer) connectTimeout);
        }
        final Object threadPoolSize = config.getProperties().get(ClientProperties.ASYNC_THREADPOOL_SIZE);
        if (threadPoolSize != null && threadPoolSize instanceof Integer && (Integer) threadPoolSize > 0) {
            final String name = HttpClient.class.getSimpleName() + "@" + hashCode();
            final QueuedThreadPool threadPool = new QueuedThreadPool((Integer) threadPoolSize);
            threadPool.setName(name);
            client.setExecutor(threadPool);
        }
        Boolean disableCookies = (Boolean) config.getProperties().get(JettyClientProperties.DISABLE_COOKIES);
        disableCookies = (disableCookies != null) ? disableCookies : false;

        final AuthenticationStore auth = client.getAuthenticationStore();
        final Object basicAuthProvider = config.getProperty(JettyClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION);
        if (basicAuthProvider != null && (basicAuthProvider instanceof BasicAuthentication)) {
            auth.addAuthentication((BasicAuthentication) basicAuthProvider);
        }

        final Optional<ClientProxy> proxy = ClientProxy.proxyFromConfiguration(config);
        proxy.ifPresent(clientProxy -> {
            final ProxyConfiguration proxyConfig = client.getProxyConfiguration();
            final URI u = clientProxy.uri();
            proxyConfig.addProxy(new HttpProxy(u.getHost(), u.getPort()));

            if (clientProxy.userName() != null) {
                auth.addAuthentication(new BasicAuthentication(u, "<<ANY_REALM>>",
                        clientProxy.userName(), clientProxy.password()));
            }
        });

        if (disableCookies) {
            client.setHttpCookieStore(new HttpCookieStore.Empty());
        }

        final Object slResponseMaxSize = configuration.getProperties()
            .get(JettyClientProperties.SYNC_LISTENER_RESPONSE_MAX_SIZE);
        if (slResponseMaxSize != null && slResponseMaxSize instanceof Integer
            && (Integer) slResponseMaxSize > 0) {
            this.syncListenerResponseMaxSize = Optional.of((Integer) slResponseMaxSize);
        }
        else {
            this.syncListenerResponseMaxSize = Optional.empty();
        }

        try {
            client.start();
        } catch (final Exception e) {
            throw new ProcessingException("Failed to start the client.", e);
        }
        this.cookieStore = client.getHttpCookieStore();
    }

    /**
     * Get the {@link HttpClient}.
     *
     * @return the {@link HttpClient}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public HttpClient getHttpClient() {
        return client;
    }

    /**
     * Get the {@link CookieStore}.
     *
     * @return the {@link CookieStore} instance or null when
     * JettyClientProperties.DISABLE_COOKIES set to true.
     */
    public HttpCookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public ClientResponse apply(final ClientRequest jerseyRequest) throws ProcessingException {
        final Request jettyRequest = translateRequest(jerseyRequest);
        final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(jerseyRequest.getHeaders(), jettyRequest);
        final Request.Content entity = getBytesProvider(jerseyRequest);
        if (entity != null) {
            jettyRequest.body(entity);
        }

        try {
            final ContentResponse jettyResponse;
            if (!syncListenerResponseMaxSize.isPresent()) {
                jettyResponse = jettyRequest.send();
            }
            else {
                final FutureResponseListener listener
                    = new FutureResponseListener(jettyRequest, syncListenerResponseMaxSize.get());
                jettyRequest.send(listener);
                jettyResponse = listener.get();
            }
            HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, jerseyRequest.getHeaders(),
                                           JettyConnector.this.getClass().getName(), jerseyRequest.getConfiguration());

            final jakarta.ws.rs.core.Response.StatusType status = jettyResponse.getReason() == null
                    ? Statuses.from(jettyResponse.getStatus())
                    : Statuses.from(jettyResponse.getStatus(), jettyResponse.getReason());

            final ClientResponse jerseyResponse = new ClientResponse(status, jerseyRequest);
            processResponseHeaders(jettyResponse.getHeaders(), jerseyResponse);
            try {
                jerseyResponse.setEntityStream(new HttpClientResponseInputStream(jettyResponse));
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }

            return jerseyResponse;
        } catch (final Exception e) {
            throw new ProcessingException(e);
        }
    }

    private static void processResponseHeaders(final HttpFields respHeaders, final ClientResponse jerseyResponse) {
        for (final HttpField header : respHeaders) {
            final String headerName = header.getName();
            final MultivaluedMap<String, String> headers = jerseyResponse.getHeaders();
            List<String> list = headers.get(headerName);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(header.getValue());
            headers.put(headerName, list);
        }
    }

    private static final class HttpClientResponseInputStream extends FilterInputStream {

        HttpClientResponseInputStream(final ContentResponse jettyResponse) throws IOException {
            super(getInputStream(jettyResponse));
        }

        private static InputStream getInputStream(final ContentResponse response) {
            return new ByteArrayInputStream(response.getContent());
        }
    }

    private Request translateRequest(final ClientRequest clientRequest) {

        final URI uri = clientRequest.getUri();
        final Request request = client.newRequest(uri);
        request.method(clientRequest.getMethod());

        request.followRedirects(clientRequest.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, true));
        final Object readTimeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, -1);
        if (readTimeout != null && readTimeout instanceof Integer && (Integer) readTimeout > 0) {
            request.idleTimeout((Integer) readTimeout, TimeUnit.MILLISECONDS);
        }

        final Object totalTimeout = clientRequest.resolveProperty(JettyClientProperties.TOTAL_TIMEOUT, -1);
        if (totalTimeout != null && totalTimeout instanceof Integer && (Integer) totalTimeout > 0) {
            request.timeout((Integer) totalTimeout, TimeUnit.MILLISECONDS);
        }

        return request;
    }

    private Map<String, String> writeOutBoundHeaders(final MultivaluedMap<String, Object> headers, final Request request) {
        final Map<String, String> stringHeaders = HeaderUtils.asStringHeadersSingleValue(headers, configuration);

        // remove User-agent header set by Jetty; Jersey already sets this in its request (incl. Jetty version)
        request.headers(httpFields -> httpFields.remove(HttpHeader.USER_AGENT));
        if (request instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) request;
            for (final Map.Entry<String, String> e : stringHeaders.entrySet()) {
                httpRequest.addHeader(new HttpField(e.getKey(), e.getValue()));
            }
        }
        return stringHeaders;
    }

    private Request.Content getBytesProvider(final ClientRequest clientRequest) {
        final Object entity = clientRequest.getEntity();

        if (entity == null) {
            return null;
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
            @Override
            public OutputStream getOutputStream(final int contentLength) throws IOException {
                return outputStream;
            }
        });

        try {
            clientRequest.writeEntity();
        } catch (final IOException e) {
            throw new ProcessingException("Failed to write request entity.", e);
        }
        return new ByteBufferRequestContent(ByteBuffer.wrap(outputStream.toByteArray()));
    }

    private Request.Content getStreamProvider(final ClientRequest clientRequest) {
        final Object entity = clientRequest.getEntity();

        if (entity == null) {
            return null;
        }

        String contentTypeHeader = clientRequest.getRequestHeaders()
                .getFirst(HttpHeader.CONTENT_TYPE.asString());

        String contentType = contentTypeHeader != null ? contentTypeHeader : "application/octet-stream";

        final OutputStreamRequestContent streamContentProvider = new OutputStreamRequestContent(contentType);
        clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
            @Override
            public OutputStream getOutputStream(final int contentLength) throws IOException {
                return streamContentProvider.getOutputStream();
            }
        });
        return streamContentProvider;
    }

    private void processContent(final ClientRequest clientRequest, final Request.Content entity) throws IOException {
        if (entity == null) {
            return;
        }

        final OutputStreamRequestContent streamContentProvider = (OutputStreamRequestContent) entity;
        try (final OutputStream output = streamContentProvider.getOutputStream()) {
            clientRequest.writeEntity();
        }
    }

    @Override
    public Future<?> apply(final ClientRequest jerseyRequest, final AsyncConnectorCallback callback) {
        final Request jettyRequest = translateRequest(jerseyRequest);
        final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(jerseyRequest.getHeaders(), jettyRequest);
        final Request.Content entity = getStreamProvider(jerseyRequest);
        if (entity != null) {
            jettyRequest.body(entity);
        }
        final AtomicBoolean callbackInvoked = new AtomicBoolean(false);
        final Throwable failure;
        try {
            final CompletableFuture<ClientResponse> responseFuture = new CompletableFuture<ClientResponse>();
            responseFuture.whenComplete(
                            (clientResponse, throwable) -> {
                                if (throwable != null && throwable instanceof CancellationException) {
                                    // take care of future cancellation
                                    jettyRequest.abort(throwable);

                                }
                            });

            final AtomicReference<ClientResponse> jerseyResponse = new AtomicReference<>();
            final ByteBufferInputStream entityStream = new ByteBufferInputStream();
            jettyRequest.send(new Response.Listener() {

                @Override
                public void onHeaders(final Response jettyResponse) {
                    HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, jerseyRequest.getHeaders(),
                                                   JettyConnector.this.getClass().getName(), jerseyRequest.getConfiguration());

                    if (responseFuture.isDone()) {
                        if (!callbackInvoked.compareAndSet(false, true)) {
                            return;
                        }
                    }
                    final ClientResponse response = translateResponse(jerseyRequest, jettyResponse, entityStream);
                    jerseyResponse.set(response);
                }

                @Override
                public void onContent(final Response jettyResponse, final ByteBuffer content) {
                    try {
                        // content must be consumed before returning from this method.

                        if (content.hasArray()) {
                            byte[] array = content.array();
                            byte[] buff = new byte[content.remaining()];
                            System.arraycopy(array, content.arrayOffset(), buff, 0, content.remaining());
                            entityStream.put(ByteBuffer.wrap(buff));
                        } else {
                            byte[] buff = new byte[content.remaining()];
                            content.get(buff);
                            entityStream.put(ByteBuffer.wrap(buff));
                        }
                    } catch (final InterruptedException ex) {
                        final ProcessingException pe = new ProcessingException(ex);
                        entityStream.closeQueue(pe);
                        // try to complete the future with an exception
                        responseFuture.completeExceptionally(pe);
                        Thread.currentThread().interrupt();
                    }
                }

                @Override
                public void onComplete(final Result result) {
                    entityStream.closeQueue();
                    if (!callbackInvoked.get()) {
                        callback.response(jerseyResponse.get());
                    }
                    responseFuture.complete(jerseyResponse.get());
                }

                @Override
                public void onFailure(final Response response, final Throwable t) {
                    entityStream.closeQueue(t);
                    // try to complete the future with an exception
                    responseFuture.completeExceptionally(t);
                    if (callbackInvoked.compareAndSet(false, true)) {
                        callback.failure(t);
                    }
                }
            });
            processContent(jerseyRequest, entity);
            return responseFuture;
        } catch (final Throwable t) {
            failure = t;
        }

        if (callbackInvoked.compareAndSet(false, true)) {
            callback.failure(failure);
        }
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.completeExceptionally(failure);
        return future;
    }

    private static ClientResponse translateResponse(final ClientRequest jerseyRequest,
                                                    final org.eclipse.jetty.client.Response jettyResponse,
                                                    final NonBlockingInputStream entityStream) {
        final ClientResponse jerseyResponse = new ClientResponse(Statuses.from(jettyResponse.getStatus()), jerseyRequest);
        processResponseHeaders(jettyResponse.getHeaders(), jerseyResponse);
        jerseyResponse.setEntityStream(entityStream);
        return jerseyResponse;
    }

    @Override
    public String getName() {
        return "Jetty HttpClient " + Jetty.VERSION;
    }

    @Override
    public void close() {
        try {
            client.stop();
        } catch (final Exception e) {
            throw new ProcessingException("Failed to stop the client.", e);
        }
    }
}
