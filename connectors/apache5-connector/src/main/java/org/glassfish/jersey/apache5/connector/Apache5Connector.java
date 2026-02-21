/*
 * Copyright (c) 2022, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.io.entity.BufferedHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.VersionInfo;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.innate.http.SSLParamConfigurator;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.innate.io.InputStreamWrapper;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.message.internal.Statuses;

/**
 * A {@link Connector} that utilizes the Apache HTTP Client to send and receive
 * HTTP request and responses.
 * <p/>
 * The following properties are only supported at construction of this class:
 * <ul>
 * <li>{@link Apache5ClientProperties#CONNECTION_CLOSING_STRATEGY}</li>
 * <li>{@link Apache5ClientProperties#CONNECTION_MANAGER}</li>
 * <li>{@link Apache5ClientProperties#CONNECTION_MANAGER_SHARED}</li>
 * <li>{@link Apache5ClientProperties#DISABLE_COOKIES}</li>
 * <li>{@link Apache5ClientProperties#CREDENTIALS_PROVIDER}</li>
 * <li>{@link Apache5ClientProperties#KEEPALIVE_STRATEGY}</li>
 * <li>{@link Apache5ClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_URI}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_USERNAME}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#PROXY_PASSWORD}</li>
 * <li>{@link org.glassfish.jersey.client.ClientProperties#REQUEST_ENTITY_PROCESSING} - default value is {@link org.glassfish.jersey.client.RequestEntityProcessing#CHUNKED}</li>
 * <li>{@link Apache5ClientProperties#REQUEST_CONFIG}</li>
 * <li>{@link Apache5ClientProperties#RETRY_STRATEGY}</li>
 * <li>{@link Apache5ClientProperties#REUSE_STRATEGY}</li>
 * <li>{@link Apache5ClientProperties#USE_SYSTEM_PROPERTIES}</li>
 * </ul>
 * <p>
 * This connector uses {@link RequestEntityProcessing#CHUNKED chunked encoding} as a default setting. This can
 * be overridden by the {@link ClientProperties#REQUEST_ENTITY_PROCESSING}. By default the
 * {@link ClientProperties#CHUNKED_ENCODING_SIZE} property is only supported by using default connection manager. If custom
 * connection manager needs to be used then chunked encoding size can be set by providing a custom
 * {@link org.apache.hc.core5.http.io.HttpClientConnection} (via custom {@link org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory})
 * and overriding {@code createOutputStream} method.
 * </p>
 * <p>
 * Using of authorization is dependent on the chunk encoding setting. If the entity
 * buffering is enabled, the entity is buffered and authorization can be performed
 * automatically in response to a 401 by sending the request again. When entity buffering
 * is disabled (chunked encoding is used) then the property
 * {@link Apache5ClientProperties#PREEMPTIVE_BASIC_AUTHENTICATION} must
 * be set to {@code true}.
 * </p>
 * <p>
 * Registration of {@link Apache5HttpClientBuilderConfigurator} instance on the
 * {@link jakarta.ws.rs.client.Client#register(Object) Client} is supported. A configuration provided by
 * {@link Apache5HttpClientBuilderConfigurator} will override the {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}
 * configuration set by using the properties.
 * </p>
 * <p>
 * If a {@link org.glassfish.jersey.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link org.glassfish.jersey.client.ClientResponse#close()} MUST be called
 * after processing the response to release connection-based resources.
 * </p>
 * <p>
 * Client operations are thread safe, the HTTP connection may
 * be shared between different threads.
 * </p>
 * <p>
 * If a response entity is obtained that is an instance of {@link Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * </p>
 * <p>
 * The following methods are currently supported: HEAD, GET, POST, PUT, DELETE, OPTIONS, PATCH and TRACE.
 * </p>
 *
 * @author jorgeluisw@mac.com
 * @author Paul Sandoz
 * @author Pavel Bucek
 * @author Arul Dhesiaseelan (aruld at acm.org)
 * @author Steffen Nießing
 * @see Apache5ClientProperties#CONNECTION_MANAGER
 */
class Apache5Connector implements Connector {

    private static final Logger LOGGER = Logger.getLogger(Apache5Connector.class.getName());
    private static final String JERSEY_REQUEST_ATTR_NAME = "JerseyRequestAttribute";
    private static final VersionInfo vi;
    private static final String release;

    static {
        vi = VersionInfo.loadVersionInfo("org.apache.hc.client5", HttpClientBuilder.class.getClassLoader());
        release = (vi != null) ? vi.getRelease() : VersionInfo.UNAVAILABLE;
    }

    private final CloseableHttpClient client;
    private final CookieStore cookieStore;
    private final boolean preemptiveBasicAuth;
    private final RequestConfig requestConfig;

    /**
     * Create the new Apache HTTP Client connector.
     *
     * @param client JAX-RS client instance for which the connector is being created.
     * @param config client configuration.
     */
    Apache5Connector(final Client client, final Configuration config) {
        final Object connectionManager = config.getProperties().get(Apache5ClientProperties.CONNECTION_MANAGER);
        if (connectionManager != null) {
            if (!(connectionManager instanceof HttpClientConnectionManager)) {
                LOGGER.log(
                        Level.WARNING,
                        LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                Apache5ClientProperties.CONNECTION_MANAGER,
                                connectionManager.getClass().getName(),
                                HttpClientConnectionManager.class.getName())
                );
            }
        }

        Object keepAliveStrategy = config.getProperties().get(Apache5ClientProperties.KEEPALIVE_STRATEGY);
        if (keepAliveStrategy != null) {
            if (!(keepAliveStrategy instanceof ConnectionKeepAliveStrategy)) {
                LOGGER.log(
                        Level.WARNING,
                        LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                Apache5ClientProperties.KEEPALIVE_STRATEGY,
                                keepAliveStrategy.getClass().getName(),
                                ConnectionKeepAliveStrategy.class.getName())
                );
                keepAliveStrategy = null;
            }
        }

        Object reuseStrategy = config.getProperties().get(Apache5ClientProperties.REUSE_STRATEGY);
        if (reuseStrategy != null) {
            if (!(reuseStrategy instanceof ConnectionReuseStrategy)) {
                LOGGER.log(
                        Level.WARNING,
                        LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                Apache5ClientProperties.REUSE_STRATEGY,
                                reuseStrategy.getClass().getName(),
                                ConnectionReuseStrategy.class.getName())
                );
                reuseStrategy = null;
            }
        }

        Object reqConfig = config.getProperties().get(Apache5ClientProperties.REQUEST_CONFIG);
        if (reqConfig != null) {
            if (!(reqConfig instanceof RequestConfig)) {
                LOGGER.log(
                        Level.WARNING,
                        LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                Apache5ClientProperties.REQUEST_CONFIG,
                                reqConfig.getClass().getName(),
                                RequestConfig.class.getName())
                );
                reqConfig = null;
            }
        }

        final boolean useSystemProperties =
                PropertiesHelper.isProperty(config.getProperties(), Apache5ClientProperties.USE_SYSTEM_PROPERTIES);

        final SSLContext sslContext = client.getSslContext();
        final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        if (useSystemProperties) {
            clientBuilder.useSystemProperties();
        }

        clientBuilder.setConnectionManager(getConnectionManager(client, config, sslContext, useSystemProperties));
        clientBuilder.setConnectionManagerShared(
                PropertiesHelper.getValue(
                        config.getProperties(),
                        Apache5ClientProperties.CONNECTION_MANAGER_SHARED,
                        false,
                        null
                )
        );
        if (keepAliveStrategy != null) {
            clientBuilder.setKeepAliveStrategy((ConnectionKeepAliveStrategy) keepAliveStrategy);
        }
        if (reuseStrategy != null) {
            clientBuilder.setConnectionReuseStrategy((ConnectionReuseStrategy) reuseStrategy);
        }

        final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        final Object credentialsProvider = config.getProperty(Apache5ClientProperties.CREDENTIALS_PROVIDER);
        if (credentialsProvider != null && (credentialsProvider instanceof CredentialsProvider)) {
            clientBuilder.setDefaultCredentialsProvider((CredentialsProvider) credentialsProvider);
        }

        final Object retryHandler = config.getProperties().get(Apache5ClientProperties.RETRY_STRATEGY);
        if (retryHandler != null && (retryHandler instanceof HttpRequestRetryStrategy)) {
            clientBuilder.setRetryStrategy((HttpRequestRetryStrategy) retryHandler);
        }

        final Optional<ClientProxy> proxy = ClientProxy.proxyFromConfiguration(config);
        proxy.ifPresent(clientProxy -> {
            final URI u = clientProxy.uri();
            final HttpHost proxyHost = new HttpHost(u.getScheme(), u.getHost(), u.getPort());
            if (clientProxy.userName() != null && clientProxy.password() != null) {
                final CredentialsStore credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(u.getHost(), u.getPort()),
                        new UsernamePasswordCredentials(clientProxy.userName(), clientProxy.password().toCharArray())
                );
                clientBuilder.setDefaultCredentialsProvider(credsProvider);
            }
            clientBuilder.setProxy(proxyHost);
        });

        final Boolean preemptiveBasicAuthProperty = (Boolean) config.getProperties()
                .get(Apache5ClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION);
        this.preemptiveBasicAuth = (preemptiveBasicAuthProperty != null) ? preemptiveBasicAuthProperty : false;

        final boolean ignoreCookies = PropertiesHelper.isProperty(
                config.getProperties(),
                Apache5ClientProperties.DISABLE_COOKIES
        );

        if (reqConfig != null) {
            final RequestConfig.Builder reqConfigBuilder = RequestConfig.copy((RequestConfig) reqConfig);
            if (ignoreCookies) {
                reqConfigBuilder.setCookieSpec(StandardCookieSpec.IGNORE);
            }
            requestConfig = reqConfigBuilder.build();
        } else {
            if (ignoreCookies) {
                requestConfigBuilder.setCookieSpec(StandardCookieSpec.IGNORE);
            }
            requestConfig = requestConfigBuilder.build();
        }

        if (requestConfig.getCookieSpec() == null || !requestConfig.getCookieSpec().equals(StandardCookieSpec.IGNORE)) {
            this.cookieStore = new BasicCookieStore();
            clientBuilder.setDefaultCookieStore(cookieStore);
        } else {
            this.cookieStore = null;
        }
        clientBuilder.setDefaultRequestConfig(requestConfig);

        LinkedList<Object> contracts = config.getInstances().stream()
                .filter(Apache5HttpClientBuilderConfigurator.class::isInstance)
                .collect(Collectors.toCollection(LinkedList::new));

        HttpClientBuilder configuredBuilder = clientBuilder;
        for (Object configurator : contracts) {
            configuredBuilder = ((Apache5HttpClientBuilderConfigurator) configurator).configure(configuredBuilder);
        }

        this.client = configuredBuilder.build();
    }

    private HttpClientConnectionManager getConnectionManager(final Client client,
                                                             final Configuration config,
                                                             final SSLContext sslContext,
                                                             final boolean useSystemProperties) {
        final Object cmObject = config.getProperties().get(Apache5ClientProperties.CONNECTION_MANAGER);

        // Connection manager from configuration.
        if (cmObject != null) {
            if (cmObject instanceof HttpClientConnectionManager) {
                return (HttpClientConnectionManager) cmObject;
            } else {
                LOGGER.log(
                        Level.WARNING,
                        LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                Apache5ClientProperties.CONNECTION_MANAGER,
                                cmObject.getClass().getName(),
                                HttpClientConnectionManager.class.getName())
                );
            }
        }

        // Create custom connection manager.
        return createConnectionManager(
                client,
                config,
                sslContext,
            useSystemProperties);
    }

    private HttpClientConnectionManager createConnectionManager(
            final Client client,
            final Configuration config,
            final SSLContext sslContext,
            final boolean useSystemProperties) {

        final String[] supportedProtocols = useSystemProperties ? split(
                System.getProperty("https.protocols")) : null;
        final String[] supportedCipherSuites = useSystemProperties ? split(
                System.getProperty("https.cipherSuites")) : null;

        HostnameVerifier hostnameVerifier = client.getHostnameVerifier();

        final LayeredConnectionSocketFactory sslSocketFactory;
        if (sslContext != null) {
            sslSocketFactory = new SniSSLConnectionSocketFactory(
                    sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
        } else {
            if (useSystemProperties) {
                sslSocketFactory = new SniSSLConnectionSocketFactory(
                        (SSLSocketFactory) SSLSocketFactory.getDefault(),
                        supportedProtocols, supportedCipherSuites, hostnameVerifier);
            } else {
                sslSocketFactory = new SniSSLConnectionSocketFactory(
                        SSLContexts.createDefault(),
                        hostnameVerifier);
            }
        }

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        final Integer chunkSize = ClientProperties.getValue(config.getProperties(),
                ClientProperties.CHUNKED_ENCODING_SIZE, ClientProperties.DEFAULT_CHUNK_SIZE, Integer.class);

        final PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(registry, new ConnectionFactory(chunkSize));

        if (useSystemProperties) {
            String s = System.getProperty("http.keepAlive", "true");
            if ("true".equalsIgnoreCase(s)) {
                s = System.getProperty("http.maxConnections", "5");
                final int max = Integer.parseInt(s);
                connectionManager.setDefaultMaxPerRoute(max);
                connectionManager.setMaxTotal(2 * max);
            }
        }

        return connectionManager;
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
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
     * @return the {@link CookieStore} instance or {@code null} when {@value Apache5ClientProperties#DISABLE_COOKIES} set to
     * {@code true}.
     */
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public ClientResponse apply(final ClientRequest clientRequest) throws ProcessingException {
        final HttpUriRequest request = getUriHttpRequest(clientRequest);
        final Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest, request);

        try {
            final CloseableHttpResponse response;
            final HttpClientContext context = HttpClientContext.create();
            final HttpHost httpHost = getHost(request);

            // If a request-specific CredentialsProvider exists, use it instead of the default one
            CredentialsProvider credentialsProvider =
                    clientRequest.resolveProperty(Apache5ClientProperties.CREDENTIALS_PROVIDER, CredentialsProvider.class);
            if (credentialsProvider != null) {
                context.setCredentialsProvider(credentialsProvider);
            }

            if (preemptiveBasicAuth) {
                final AuthCache authCache = new BasicAuthCache();
                final BasicScheme basicScheme = new BasicScheme();
                final AuthScope authScope = new AuthScope(httpHost);
                basicScheme.initPreemptive(credentialsProvider.getCredentials(authScope, context));
                context.resetAuthExchange(httpHost, basicScheme);
                authCache.put(httpHost, basicScheme); // must be after initPreemptive
                context.setAuthCache(authCache);
            }

            context.setAttribute(JERSEY_REQUEST_ATTR_NAME, clientRequest);
            response = client.execute(httpHost, request, context);
            HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, clientRequest.getHeaders(),
                    this.getClass().getName(), clientRequest.getConfiguration());

            final Response.StatusType status = response.getReasonPhrase() == null
                    ? Statuses.from(response.getCode())
                    : Statuses.from(response.getCode(), response.getReasonPhrase());

            final ClientResponse responseContext = new ClientResponse(status, clientRequest);
            final List<URI> redirectLocations = context.getRedirectLocations().getAll();
            if (redirectLocations != null && !redirectLocations.isEmpty()) {
                responseContext.setResolvedRequestUri(redirectLocations.get(redirectLocations.size() - 1));
            }

            final Header[] respHeaders = response.getHeaders();
            final MultivaluedMap<String, String> headers = responseContext.getHeaders();
            for (final Header header : respHeaders) {
                final String headerName = header.getName();
                List<String> list = headers.get(headerName);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(header.getValue());
                headers.put(headerName, list);
            }

            final HttpEntity entity = response.getEntity();

            if (entity != null) {
                if (headers.get(HttpHeaders.CONTENT_LENGTH) == null && entity.getContentLength() >= 0) {
                    headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
                }

                final String contentEncoding = entity.getContentEncoding();
                if (headers.get(HttpHeaders.CONTENT_ENCODING) == null && contentEncoding != null && !contentEncoding.isEmpty()) {
                    headers.add(HttpHeaders.CONTENT_ENCODING, contentEncoding);
                }
            }

            try {
                final ConnectionClosingMechanism closingMechanism = new ConnectionClosingMechanism(clientRequest, request);
                responseContext.setEntityStream(getInputStream(response, closingMechanism, () -> clientRequest.isCancelled()));
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }

            return responseContext;
        } catch (final Exception e) {
            throw new ProcessingException(e);
        }
    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
        try {
            ClientResponse response = apply(request);
            callback.response(response);
            return CompletableFuture.completedFuture(response);
        } catch (Throwable t) {
            callback.failure(t);
            CompletableFuture<Object> future = new CompletableFuture<>();
            future.completeExceptionally(t);
            return future;
        }
    }

    @Override
    public String getName() {
        return "Apache HttpClient " + release;
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (final IOException e) {
            throw new ProcessingException(LocalizationMessages.FAILED_TO_STOP_CLIENT(), e);
        }
    }

    private HttpHost getHost(final HttpUriRequest request) throws URISyntaxException {
        return new HttpHost(request.getUri().getScheme(), request.getUri().getHost(), request.getUri().getPort());
    }

    private HttpUriRequest getUriHttpRequest(final ClientRequest clientRequest) {
        final RequestConfig.Builder requestConfigBuilder = RequestConfig.copy(requestConfig);

        final int connectTimeout = clientRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, -1);
        final int socketTimeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, -1);

        if (connectTimeout >= 0) {
            requestConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout));
        }
        if (socketTimeout >= 0) {
            requestConfigBuilder.setResponseTimeout(Timeout.ofMilliseconds(socketTimeout));
        }

        final Boolean redirectsEnabled =
                clientRequest.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, requestConfig.isRedirectsEnabled());
        requestConfigBuilder.setRedirectsEnabled(redirectsEnabled);

        final Boolean bufferingEnabled = clientRequest.resolveProperty(ClientProperties.REQUEST_ENTITY_PROCESSING,
                RequestEntityProcessing.class) == RequestEntityProcessing.BUFFERED;
        final HttpEntity entity = getHttpEntity(clientRequest, bufferingEnabled);

        HttpUriRequestBase httpUriRequestBase = new HttpUriRequestBase(clientRequest.getMethod(), clientRequest.getUri());
        httpUriRequestBase.setConfig(requestConfigBuilder.build());
        httpUriRequestBase.setEntity(entity);

        return httpUriRequestBase;
    }

    private HttpEntity getHttpEntity(final ClientRequest clientRequest, final boolean bufferingEnabled) {
        final Object entity = clientRequest.getEntity();

        if (entity == null) {
            return null;
        }

        if (HttpEntity.class.isInstance(entity)) {
            return wrapHttpEntity(clientRequest, (HttpEntity) entity);
        }

        String contentType = clientRequest.getHeaderString(HttpHeaders.CONTENT_TYPE);
        String contentEncoding = clientRequest.getHeaderString(HttpHeaders.CONTENT_ENCODING);

        final AbstractHttpEntity httpEntity = new AbstractHttpEntity(contentType, contentEncoding) {
            @Override
            public void close() throws IOException {

            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public long getContentLength() {
                return -1;
            }

            @Override
            public InputStream getContent() throws IOException, IllegalStateException {
                if (bufferingEnabled) {
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
                    writeTo(buffer);
                    return new ByteArrayInputStream(buffer.toByteArray());
                } else {
                    return null;
                }
            }

            @Override
            public void writeTo(final OutputStream outputStream) throws IOException {
                clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(final int contentLength) throws IOException {
                        return outputStream;
                    }
                });
                clientRequest.writeEntity();
            }

            @Override
            public boolean isStreaming() {
                return false;
            }
        };

        return bufferEntity(httpEntity, bufferingEnabled);
    }

    private HttpEntity wrapHttpEntity(final ClientRequest clientRequest, final HttpEntity originalEntity) {
        final boolean bufferingEnabled = BufferedHttpEntity.class.isInstance(originalEntity);

        try {
            clientRequest.setEntity(originalEntity.getContent());
        } catch (IOException e) {
            throw new ProcessingException(LocalizationMessages.ERROR_READING_HTTPENTITY_STREAM(e.getMessage()), e);
        }

        final AbstractHttpEntity httpEntity = new AbstractHttpEntity(
                originalEntity.getContentType(),
                originalEntity.getContentEncoding(),
                originalEntity.isChunked()
        ) {
            @Override
            public void close() throws IOException {

            }

            @Override
            public boolean isRepeatable() {
                return originalEntity.isRepeatable();
            }

            @Override
            public long getContentLength() {
                return originalEntity.getContentLength();
            }

            @Override
            public InputStream getContent() throws IOException, IllegalStateException {
               return originalEntity.getContent();
            }

            @Override
            public void writeTo(final OutputStream outputStream) throws IOException {
                clientRequest.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                    @Override
                    public OutputStream getOutputStream(final int contentLength) throws IOException {
                        return outputStream;
                    }
                });
                clientRequest.writeEntity();
            }

            @Override
            public boolean isStreaming() {
                return originalEntity.isStreaming();
            }
        };

        return bufferEntity(httpEntity, bufferingEnabled);
    }

    private static HttpEntity bufferEntity(HttpEntity httpEntity, boolean bufferingEnabled) {
        if (bufferingEnabled) {
            try {
                return new BufferedHttpEntity(httpEntity);
            } catch (final IOException e) {
                throw new ProcessingException(LocalizationMessages.ERROR_BUFFERING_ENTITY(), e);
            }
        } else {
            return httpEntity;
        }
    }

    private static Map<String, String> writeOutBoundHeaders(final ClientRequest clientRequest,
                                                            final HttpUriRequest request) {
        final Map<String, String> stringHeaders =
                HeaderUtils.asStringHeadersSingleValue(clientRequest.getHeaders(), clientRequest.getConfiguration());

        for (final Map.Entry<String, String> e : stringHeaders.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }
        return stringHeaders;
    }

    private static InputStream getInputStream(final CloseableHttpResponse response,
                                              final ConnectionClosingMechanism closingMechanism,
                                              final Supplier<Boolean> isCancelled) throws IOException {
        final InputStream inputStream;

        if (response.getEntity() == null) {
            inputStream = InputStream.nullInputStream();
        } else {
            final InputStream i = new CancellableInputStream(response.getEntity().getContent(), isCancelled);
            if (i.markSupported()) {
                inputStream = i;
            } else {
                inputStream = ReaderWriter.AUTOSIZE_BUFFER ? new BufferedInputStream(i)
                        : new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
            }
        }

        return closingMechanism.getEntityStream(inputStream, response);
    }

    /**
     * The way the Apache CloseableHttpResponse is to be closed.
     * See https://github.com/eclipse-ee4j/jersey/issues/4321
     * {@link Apache5ClientProperties#CONNECTION_CLOSING_STRATEGY}
     */
    private final class ConnectionClosingMechanism {
        private Apache5ConnectionClosingStrategy connectionClosingStrategy = null;
        private final ClientRequest clientRequest;
        private final HttpUriRequest apacheRequest;

        private ConnectionClosingMechanism(ClientRequest clientRequest, HttpUriRequest apacheRequest) {
            this.clientRequest = clientRequest;
            this.apacheRequest = apacheRequest;
            Object closingStrategyProperty = clientRequest
                    .resolveProperty(Apache5ClientProperties.CONNECTION_CLOSING_STRATEGY, Object.class);
            if (closingStrategyProperty != null) {
                if (Apache5ConnectionClosingStrategy.class.isInstance(closingStrategyProperty)) {
                    connectionClosingStrategy = (Apache5ConnectionClosingStrategy) closingStrategyProperty;
                } else {
                    LOGGER.log(
                            Level.WARNING,
                            LocalizationMessages.IGNORING_VALUE_OF_PROPERTY(
                                    Apache5ClientProperties.CONNECTION_CLOSING_STRATEGY,
                                    closingStrategyProperty,
                                    Apache5ConnectionClosingStrategy.class.getName())
                    );
                }
            }

            if (connectionClosingStrategy == null) {
                connectionClosingStrategy = Apache5ConnectionClosingStrategy.Apache5GracefulClosingStrategy.INSTANCE;
            }
        }

        private InputStream getEntityStream(final InputStream inputStream,
                                            final CloseableHttpResponse response) {
            InputStream filterStream = new FilterInputStream(inputStream) {
                @Override
                public void close() throws IOException {
                    connectionClosingStrategy.close(clientRequest, apacheRequest, response, in);
                }
            };
            return filterStream;
        }
    }

    private static class ConnectionFactory extends ManagedHttpClientConnectionFactory {
        private ConnectionFactory(final int chunkSize) {
            super(
                    Http1Config.custom().setChunkSizeHint(chunkSize).build(),
                    null,
                    null,
                    null,
                    DefaultContentLengthStrategy.INSTANCE,
                    DefaultContentLengthStrategy.INSTANCE
            );
        }
    }

    private static final class SniSSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        private final ThreadLocal<HttpContext> httpContexts = new ThreadLocal<>();

        public SniSSLConnectionSocketFactory(final SSLContext sslContext,
                                             final String[] supportedProtocols,
                                             final String[] supportedCipherSuites,
                                             final HostnameVerifier hostnameVerifier) {
            super(sslContext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
        }

        public SniSSLConnectionSocketFactory(final javax.net.ssl.SSLSocketFactory socketFactory,
                                             final String[] supportedProtocols,
                                             final String[] supportedCipherSuites,
                                             final HostnameVerifier hostnameVerifier) {
            super(socketFactory, supportedProtocols, supportedCipherSuites, hostnameVerifier);
        }

        public SniSSLConnectionSocketFactory(
                final SSLContext sslContext, final HostnameVerifier hostnameVerifier) {
            super(sslContext, hostnameVerifier);
        }

        /* Pre 5.2 */
        @Override
        public Socket createLayeredSocket(
                final Socket socket,
                final String target,
                final int port,
                final HttpContext context) throws IOException {
            httpContexts.set(context);
            try {
                return super.createLayeredSocket(socket, target, port, context);
            } finally {
                httpContexts.remove();
            }
        }

        /* Post 5.2 */
        public Socket createLayeredSocket(
                final Socket socket,
                final String target,
                final int port,
                final Object attachment,
                final HttpContext context) throws IOException {
            httpContexts.set(context);
            try {
                return super.createLayeredSocket(socket, target, port, attachment, context);
            } finally {
                httpContexts.remove();
            }
        }

        @Override
        protected void prepareSocket(SSLSocket socket) throws IOException {
            HttpContext context =  httpContexts.get();

            if (context != null) {
                Object objectRequest = context.getAttribute(JERSEY_REQUEST_ATTR_NAME);
                if (objectRequest != null) {
                    ClientRequest clientRequest = (ClientRequest) objectRequest;
                    SSLParamConfigurator sniConfig = SSLParamConfigurator.builder().request(clientRequest)
                            .setSNIHostName(clientRequest).build();
                    sniConfig.setSNIServerName(socket);

                    final int socketTimeout = ((ClientRequest) objectRequest).resolveProperty(ClientProperties.READ_TIMEOUT, -1);
                    if (socketTimeout >= 0) {
                        socket.setSoTimeout(socketTimeout);
                    }
                }
            }
        }
    }

    private static class CancellableInputStream extends InputStreamWrapper {
        private final InputStream in;
        private final Supplier<Boolean> isCancelled;

        private CancellableInputStream(InputStream in, Supplier<Boolean> isCancelled) {
            this.in = in;
            this.isCancelled = isCancelled;
        }

        @Override
        protected InputStream getWrapped() {
            return in;
        }

        @Override
        protected InputStream getWrappedIOE() throws IOException {
            if (isCancelled.get()) {
                throw new IOException(new CancellationException());
            }
            return in;
        }
    }

}
