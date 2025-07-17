/*
 * Copyright (c) 2011, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.innate.ClientProxy;
import org.glassfish.jersey.client.innate.http.SSLParamConfigurator;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.collection.LRU;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.internal.Statuses;

/**
 * Default client transport connector using {@link HttpURLConnection}.
 *
 * @author Marek Potociar
 */
public class HttpUrlConnector implements Connector {

    private static final Logger LOGGER = Logger.getLogger(HttpUrlConnector.class.getName());
    // Avoid multi-thread uses of HttpsURLConnection.getDefaultSSLSocketFactory() because it does not implement a
    // proper lazy-initialization. See https://github.com/jersey/jersey/issues/3293
    private static final LazyValue<SSLSocketFactory> DEFAULT_SSL_SOCKET_FACTORY =
            Values.lazy((Value<SSLSocketFactory>) () -> HttpsURLConnection.getDefaultSSLSocketFactory());
    // The list of restricted headers is extracted from sun.net.www.protocol.http.HttpURLConnection
    private static final String[] restrictedHeaders = {
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Connection", /* close is allowed */
            "Content-Length",
            "Content-Transfer-Encoding",
            "Host",
            "Keep-Alive",
            "Origin",
            "Trailer",
            "Transfer-Encoding",
            "Upgrade",
            "Via"
    };

    private static final Set<String> restrictedHeaderSet = new HashSet<>(restrictedHeaders.length);

    static {
        for (String headerName : restrictedHeaders) {
            restrictedHeaderSet.add(headerName.toLowerCase(Locale.ROOT));
        }
    }

    private final HttpUrlConnectorConfiguration.ReadWrite clientConfig;
    private Value<SSLSocketFactory> sslSocketFactory;

    // SSLContext#getSocketFactory not idempotent
    // JDK KeepAliveCache keeps connections per Factory
    // SSLContext set per request blows that -> keep factory in LRU
    private final LRU<SSLContext, SSLSocketFactory> sslSocketFactoryCache = LRU.create();

    private final ConnectorExtension<HttpURLConnection, IOException> connectorExtension
            = new HttpUrlExpect100ContinueConnectorExtension();

    /**
     * Create new {@code HttpUrlConnector} instance.
     *
     * @param client              JAX-RS client instance for which the connector is being created.
     * @param connectionFactory   {@link javax.net.ssl.HttpsURLConnection} factory to be used when creating connections.
     * @param chunkSize           chunk size to use when using HTTP chunked transfer coding.
     * @param fixLengthStreaming  specify if the {@link java.net.HttpURLConnection#setFixedLengthStreamingMode(int)
     *                            fixed-length streaming mode} on the underlying HTTP URL connection instances should be
     *                            used when sending requests.
     * @param setMethodWorkaround specify if the reflection workaround should be used to set HTTP URL connection method
     *                            name. See {@link HttpUrlConnectorProvider#SET_METHOD_WORKAROUND} for details.
     */
    public HttpUrlConnector(
            final Client client,
            final HttpUrlConnectorProvider.ConnectionFactory connectionFactory,
            final int chunkSize,
            final boolean fixLengthStreaming,
            final boolean setMethodWorkaround) {

        this(client, client.getConfiguration(),
                HttpUrlConnectorProvider.config()
                    .connectionFactory(connectionFactory)
                    .chunkSize(chunkSize)
                    .useFixedLengthStreaming(fixLengthStreaming)
                    .useSetMethodWorkaround(setMethodWorkaround)
        );
    }

    public HttpUrlConnector(Client client, Configuration configuration, HttpUrlConnectorConfiguration<?> config) {
        this.clientConfig = config.rw().fromClient(configuration);
        this.sslSocketFactory = Values.lazy(new Value<SSLSocketFactory>() {
            @Override
            public SSLSocketFactory get() {
                return client.getSslContext().getSocketFactory();
            }
        });
    }

    private static InputStream getInputStream(final HttpURLConnection uc, final ClientRequest clientRequest) throws IOException {
        return new InputStream() {
            private final UnsafeValue<InputStream, IOException> in = Values.lazy(new UnsafeValue<InputStream, IOException>() {
                @Override
                public InputStream get() throws IOException {
                    if (uc.getResponseCode() < Response.Status.BAD_REQUEST.getStatusCode()) {
                        return uc.getInputStream();
                    } else {
                        InputStream ein = uc.getErrorStream();
                        return (ein != null) ? ein : new ByteArrayInputStream(new byte[0]);
                    }
                }
            });

            private volatile boolean closed = false;

            /**
             * The motivation for this method is to straighten up a behaviour of {@link sun.net.www.http.KeepAliveStream} which
             * is used here as a backing {@link InputStream}. The problem is that its access methods (e.g., {@link
             * sun.net.www.http.KeepAliveStream#read()}) do not throw {@link IOException} if the stream is closed. This behaviour
             * contradicts with {@link InputStream} contract.
             * <p/>
             * This is a part of fix of JERSEY-2878
             * <p/>
             * Note that {@link java.io.FilterInputStream} also changes the contract of
             * {@link java.io.FilterInputStream#read(byte[], int, int)} as it doesn't state that closed stream causes an {@link
             * IOException} which might be questionable. Nevertheless, our contract is {@link InputStream} and as such, the
             * stream we're offering must comply with it.
             *
             * @throws IOException when the stream is closed.
             */
            private void throwIOExceptionIfClosed() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
                if (clientRequest.isCancelled()) {
                    close();
                    throw new IOException(new CancellationException());
                }
            }

            @Override
            public int read() throws IOException {
                int result = in.get().read();
                throwIOExceptionIfClosed();
                return result;
            }

            @Override
            public int read(byte[] b) throws IOException {
                int result = in.get().read(b);
                throwIOExceptionIfClosed();
                return result;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int result = in.get().read(b, off, len);
                throwIOExceptionIfClosed();
                return result;
            }

            @Override
            public long skip(long n) throws IOException {
                long result = in.get().skip(n);
                throwIOExceptionIfClosed();
                return result;
            }

            @Override
            public int available() throws IOException {
                int result = in.get().available();
                throwIOExceptionIfClosed();
                return result;
            }

            @Override
            public void close() throws IOException {
                try {
                    in.get().close();
                } finally {
                    closed = true;
                }
            }

            @Override
            public void mark(int readLimit) {
                try {
                    in.get().mark(readLimit);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to retrieve the underlying input stream.", e);
                }
            }

            @Override
            public void reset() throws IOException {
                in.get().reset();
                throwIOExceptionIfClosed();
            }

            @Override
            public boolean markSupported() {
                try {
                    return in.get().markSupported();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to retrieve the underlying input stream.", e);
                }
            }
        };
    }

    @Override
    public ClientResponse apply(ClientRequest request) {
        try {
            return _apply(request);
        } catch (IOException ex) {
            throw new ProcessingException(ex);
        }
    }

    @Override
    public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {

        try {
            callback.response(_apply(request));
        } catch (IOException ex) {
            callback.failure(new ProcessingException(ex));
        } catch (Throwable t) {
            callback.failure(t);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() {
        // do nothing
    }

    /**
     * Secure connection if necessary.
     * <p/>
     * Provided implementation sets {@link HostnameVerifier} and {@link SSLSocketFactory} to give connection, if that
     * is an instance of {@link HttpsURLConnection}.
     *
     * @param client client associated with this client runtime.
     * @param uc     http connection to be secured.
     */
    protected void secureConnection(final JerseyClient client, final HttpURLConnection uc) {
        if (uc instanceof HttpsURLConnection) {
            HttpsURLConnection suc = (HttpsURLConnection) uc;

            final HostnameVerifier verifier = client.getHostnameVerifier();
            if (verifier != null) {
                suc.setHostnameVerifier(verifier);
            }

            if (DEFAULT_SSL_SOCKET_FACTORY.get() == suc.getSSLSocketFactory()) {
                // indicates that the custom socket factory was not set
                suc.setSSLSocketFactory(sslSocketFactory.get());
            }
        }
    }

    /**
     * Secure connection if necessary.
     * <p/>
     * Provided implementation sets {@link HostnameVerifier} and {@link SSLSocketFactory} to give connection, if that
     * is an instance of {@link HttpsURLConnection}.
     *
     * @param clientRequest the actual client request.
     * @param uc     http connection to be secured.
     */
    private void secureConnection(final ClientRequest clientRequest,
                                  final HttpURLConnection uc,
                                  final SSLParamConfigurator sniConfig,
                                  final HttpUrlConnectorConfiguration.ReadWrite config) {
        if (config.isSslContextSupplier() || config.isPrefixed()) {
            setSslContextFactory(clientRequest.getClient(), clientRequest, config.sslContext(clientRequest));
        } else {
            setSslContextFactory(clientRequest.getClient(), clientRequest);
        }
        secureConnection(clientRequest.getClient(), uc); // keep this for compatibility

        if (sniConfig.isSNIRequired() && uc instanceof HttpsURLConnection) { // set SNI
            HttpsURLConnection suc = (HttpsURLConnection) uc;
            SniSSLSocketFactory socketFactory = new SniSSLSocketFactory(suc.getSSLSocketFactory());
            socketFactory.setSniConfig(sniConfig);
            suc.setSSLSocketFactory(socketFactory);
        }
    }

    @Deprecated
    protected void setSslContextFactory(Client client, ClientRequest request) {
        setSslContextFactory(client, request, request.resolveProperty(ClientProperties.SSL_CONTEXT_SUPPLIER, Supplier.class));
    }

    protected void setSslContextFactory(Client client, ClientRequest request,
                                        HttpUrlConnectorConfiguration.ReadWrite requestConfig) {
        setSslContextFactory(client, request, requestConfig.sslContext(request));
    }

    private void setSslContextFactory(Client client, ClientRequest request, Supplier<SSLContext> supplier) {
        if (supplier != null) {
            sslSocketFactory = Values.lazy(new Value<SSLSocketFactory>() { // lazy for double-check locking if multiple requests
                @Override
                public SSLSocketFactory get() {
                    SSLContext sslContext = supplier.get();
                    SSLSocketFactory factory = sslSocketFactoryCache.getIfPresent(sslContext);
                    if (factory == null) {
                        factory = sslContext.getSocketFactory();
                        sslSocketFactoryCache.put(sslContext, factory);
                    }
                    return factory;
                }
            });
        }
    }

    private ClientResponse _apply(final ClientRequest request) throws IOException {
        HttpUrlConnectorConfiguration.ReadWrite requestConfiguration = clientConfig.fromRequest(request);

        final HttpURLConnection uc;
        final SSLParamConfigurator sniConfig = SSLParamConfigurator.builder(requestConfiguration).request(request)
                .setSNIHostName(request).build();
        final URI sniUri;
        if (sniConfig.isSNIRequired()) {
            sniUri = sniConfig.toIPRequestUri();
            LOGGER.fine(LocalizationMessages.SNI_URI_REPLACED(sniUri.getHost(), request.getUri().getHost()));
        } else {
            sniUri = request.getUri();
        }

        if (!DEFAULT_SSL_SOCKET_FACTORY.isInitialized() && "HTTPS".equalsIgnoreCase(sniUri.getScheme())) {
            DEFAULT_SSL_SOCKET_FACTORY.get();
        }

        final Optional<ClientProxy> proxy = requestConfiguration.proxy(request, sniUri);
        proxy.ifPresent(clientProxy -> ClientProxy.setBasicAuthorizationHeader(request.getHeaders(), proxy.get()));
        uc = ((Supplier<HttpUrlConnectorProvider.ConnectionFactory>) requestConfiguration.connectionFactory)
                .get().getConnection(sniUri.toURL(), proxy.isPresent() ? proxy.get().proxy() : null);
        uc.setDoInput(true);

        final String httpMethod = request.getMethod();
        if (requestConfiguration.isMethodWorkaround(request)) {
            setRequestMethodViaJreBugWorkaround(uc, httpMethod);
        } else {
            uc.setRequestMethod(httpMethod);
        }

        uc.setInstanceFollowRedirects(requestConfiguration.followRedirects(request));

        if (requestConfiguration.connectTimeout() == 0) {
            requestConfiguration.connectTimeout(uc.getConnectTimeout());
        }
        uc.setConnectTimeout(requestConfiguration.connectTimeout(request));

        if (requestConfiguration.readTimeout() == 0) {
            requestConfiguration.readTimeout(uc.getReadTimeout());
        }
        uc.setReadTimeout(requestConfiguration.readTimeout(request).readTimeout());

        secureConnection(request, uc, sniConfig, requestConfiguration);

        final Object entity = request.getEntity();
        Exception storedException = null;
        try {
            if (entity != null) {
                RequestEntityProcessing entityProcessing = requestConfiguration.requestEntityProcessing(request);
                final long length = request.getLengthLong();

                if (entityProcessing != RequestEntityProcessing.BUFFERED) {
                    if (requestConfiguration.useFixedLengthStreaming.get() && length > 0) {
                        uc.setFixedLengthStreamingMode(length);
                    } else if (entityProcessing == RequestEntityProcessing.CHUNKED) {
                        uc.setChunkedStreamingMode(requestConfiguration.chunkSize.get());
                    }
                }
                uc.setDoOutput(true);

                if ("GET".equalsIgnoreCase(httpMethod)) {
                    final Logger logger = Logger.getLogger(HttpUrlConnector.class.getName());
                    if (logger.isLoggable(Level.INFO)) {
                        logger.log(Level.INFO, LocalizationMessages.HTTPURLCONNECTION_REPLACES_GET_WITH_ENTITY());
                    }
                }

                processExtensions(request, uc);

                request.setStreamProvider(contentLength -> {
                    setOutboundHeaders(request.getStringHeaders(), uc, requestConfiguration);
                    return uc.getOutputStream();
                });
                request.writeEntity();

            } else {
                setOutboundHeaders(request.getStringHeaders(), uc, requestConfiguration);
            }
        } catch (IOException ioe) {
            storedException = handleException(request, ioe, uc);
        }

        final int code = uc.getResponseCode();
        final String reasonPhrase = uc.getResponseMessage();
        final Response.StatusType status =
                reasonPhrase == null ? Statuses.from(code) : Statuses.from(code, reasonPhrase);

        URI resolvedRequestUri = null;
        try {
            resolvedRequestUri = uc.getURL().toURI();
        } catch (URISyntaxException e) {
            // if there is already an exception stored, the stored exception is what matters most
            if (storedException == null) {
                storedException = e;
            } else {
                storedException.addSuppressed(e);
            }
        }

        ClientResponse responseContext = new ClientResponse(status, request, resolvedRequestUri);
        responseContext.headers(
                uc.getHeaderFields()
                  .entrySet()
                  .stream()
                  .filter(stringListEntry -> stringListEntry.getKey() != null)
                  .collect(Collectors.toMap(Map.Entry::getKey,
                                            Map.Entry::getValue))
        );

        try {
            InputStream inputStream = getInputStream(uc, request);
            responseContext.setEntityStream(inputStream);
        } catch (IOException ioe) {
            // allow at least a partial response in a ResponseProcessingException
            if (storedException == null) {
                storedException = ioe;
            } else {
                storedException.addSuppressed(ioe);
            }
        }

        if (storedException != null) {
            throw new ClientResponseProcessingException(responseContext, storedException);
        }

        return responseContext;
    }

    private void setOutboundHeaders(MultivaluedMap<String, String> headers,
                                    HttpURLConnection uc,
                                    HttpUrlConnectorConfiguration.ReadWrite requestConfiguration) {
        boolean restrictedSent = false;
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String headerName = header.getKey();
            String headerValue;

            List<String> headerValues = header.getValue();
            if (headerValues.size() == 1) {
                headerValue = headerValues.get(0);
                uc.setRequestProperty(headerName, headerValue);
            } else {
                StringBuilder b = new StringBuilder();
                boolean add = false;
                for (Object value : headerValues) {
                    if (add) {
                        b.append(',');
                    }
                    add = true;
                    b.append(value);
                }
                headerValue = b.toString();
                uc.setRequestProperty(headerName, headerValue);
            }
            // if (at least one) restricted header was added and the allowRestrictedHeaders
            if (!requestConfiguration.isRestrictedHeaderPropertySet.get() && !restrictedSent) {
                if (isHeaderRestricted(headerName, headerValue)) {
                    restrictedSent = true;
                }
            }
        }
        if (restrictedSent) {
            LOGGER.warning(LocalizationMessages.RESTRICTED_HEADER_POSSIBLY_IGNORED(
                    HttpUrlConnectorConfiguration.ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY));
        }
    }

    private boolean isHeaderRestricted(String name, String value) {
        name = name.toLowerCase(Locale.ROOT);
        return name.startsWith("sec-")
                || restrictedHeaderSet.contains(name)
                && !("connection".equalsIgnoreCase(name) && "close".equalsIgnoreCase(value));
    }

    /**
     * Workaround for a bug in {@code HttpURLConnection.setRequestMethod(String)}
     * The implementation of Sun/Oracle is throwing a {@code ProtocolException}
     * when the method is not in the list of the HTTP/1.1 default methods.
     * This means that to use e.g. {@code PROPFIND} and others, we must apply this workaround.
     * <p/>
     * See issue http://java.net/jira/browse/JERSEY-639
     */
    private static void setRequestMethodViaJreBugWorkaround(final HttpURLConnection httpURLConnection,
                                                            final String method) {
        try {
            httpURLConnection.setRequestMethod(method); // Check whether we are running on a buggy JRE
        } catch (final ProtocolException pe) {
            try {
                AccessController
                        .doPrivileged(new PrivilegedExceptionAction<Object>() {
                            @Override
                            public Object run() throws NoSuchFieldException,
                                    IllegalAccessException {
                                try {
                                    httpURLConnection.setRequestMethod(method);
                                    // Check whether we are running on a buggy
                                    // JRE
                                } catch (final ProtocolException pe) {
                                    Class<?> connectionClass = httpURLConnection
                                            .getClass();
                                    try {
                                        final Field delegateField = connectionClass.getDeclaredField("delegate");
                                        delegateField.setAccessible(true);

                                        HttpURLConnection delegateConnection =
                                                (HttpURLConnection) delegateField.get(httpURLConnection);
                                        setRequestMethodViaJreBugWorkaround(delegateConnection, method);
                                    } catch (NoSuchFieldException e) {
                                        // Ignore for now, keep going
                                    } catch (IllegalArgumentException | IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        Field methodField;
                                        while (connectionClass != null) {
                                            try {
                                                methodField = connectionClass
                                                        .getDeclaredField("method");
                                            } catch (NoSuchFieldException e) {
                                                connectionClass = connectionClass
                                                        .getSuperclass();
                                                continue;
                                            }
                                            methodField.setAccessible(true);
                                            methodField.set(httpURLConnection, method);
                                            break;
                                        }
                                    } catch (final Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                return null;
                            }
                        });
            } catch (final PrivilegedActionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        }
    }

    private void processExtensions(ClientRequest request, HttpURLConnection uc) {
        connectorExtension.invoke(request, uc);
    }

    private IOException handleException(ClientRequest request, IOException ex, HttpURLConnection uc) throws IOException {
        if (connectorExtension.handleException(request, uc, ex)) {
            return null;
        }
        /*
         *  uc.getResponseCode triggers another request. If we already know it is a SocketTimeoutException
         *  we can throw the exception directly. Otherwise the request will be 2 * timeout.
         */
        if (ex instanceof SocketTimeoutException || uc.getResponseCode() == -1) {
            throw ex;
        } else {
            return ex;
        }
    }

    @Override
    public String getName() {
        return "HttpUrlConnection " + AccessController.doPrivileged(PropertiesHelper.getSystemProperty("java.version"));
    }

    private static class SniSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory socketFactory;
        private final ThreadLocal<SSLParamConfigurator> sniConfigs = new ThreadLocal<>();

        public void setSniConfig(SSLParamConfigurator sniConfigs) {
            this.sniConfigs.set(sniConfigs);
        }

        private SniSSLSocketFactory(SSLSocketFactory socketFactory) {
            this.socketFactory = socketFactory;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return socketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return socketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
            Socket superSocket = socketFactory.createSocket(socket, s, i, b);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
            Socket superSocket = socketFactory.createSocket(s, i);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
            Socket superSocket = socketFactory.createSocket(s, i, inetAddress, i1);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            Socket superSocket = socketFactory.createSocket(inetAddress, i);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
            Socket superSocket = socketFactory.createSocket(inetAddress, i, inetAddress1, i1);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
            Socket superSocket = socketFactory.createSocket(s, consumed, autoClose);
            setSNIServerName(superSocket);
            return superSocket;
        }

        @Override
        public Socket createSocket() throws IOException {
            Socket superSocket = socketFactory.createSocket();
            setSNIServerName(superSocket);
            return superSocket;
        }

        private void setSNIServerName(Socket superSocket) {
            SSLParamConfigurator sniConfig = this.sniConfigs.get();
            if (null != sniConfig && SSLSocket.class.isInstance(superSocket)) {
                sniConfig.setSNIServerName((SSLSocket) superSocket);
            }
            this.sniConfigs.remove();
        }
    }
}
