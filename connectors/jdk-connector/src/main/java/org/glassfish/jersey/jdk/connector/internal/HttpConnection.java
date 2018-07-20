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
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpConnection {

    /**
     * Input buffer that is used by {@link TransportFilter} when SSL is turned on.
     * The size cannot be smaller than a maximal size of a SSL packet, which is 16kB for payload + header, because
     * {@link SslFilter} does not have its own buffer for buffering incoming
     * data and therefore the entire SSL packet must fit into {@link SslFilter}
     * input buffer.
     * <p/>
     */
    private static final int SSL_INPUT_BUFFER_SIZE = 17_000;
    /**
     * Input buffer that is used by {@link TransportFilter} when SSL is not turned on.
     */
    private static final int INPUT_BUFFER_SIZE = 2048;

    private static final Logger LOGGER = Logger.getLogger(HttpConnection.class.getName());

    private final Filter<HttpRequest, HttpResponse, HttpRequest, HttpResponse> filterChain;
    private final CookieManager cookieManager;
    // we are interested only in host-port pair, but URI is a convenient holder for it
    private final URI uri;
    private final StateChangeListener stateListener;
    private final ScheduledExecutorService scheduler;
    private final ConnectorConfiguration configuration;

    private HttpRequest httpRequest;
    private HttpResponse httResponse;
    private Throwable error;
    volatile State state = State.CREATED;

    // by default we treat all connection as persistent
    // this flag will change to false if we receive "Connection: Close" header
    private boolean persistentConnection = true;

    private Future<?> responseTimeout;
    private Future<?> idleTimeout;
    private Future<?> connectTimeout;

    HttpConnection(URI uri,
                   CookieManager cookieManager,
                   ConnectorConfiguration configuration,
                   ScheduledExecutorService scheduler,
                   StateChangeListener stateListener) {
        this.uri = uri;
        this.cookieManager = cookieManager;
        this.stateListener = stateListener;
        this.configuration = configuration;
        this.scheduler = scheduler;
        filterChain = createFilterChain(uri, configuration);
    }

    synchronized void connect() {
        if (state != State.CREATED) {
            throw new IllegalStateException(LocalizationMessages.HTTP_CONNECTION_ESTABLISHING_ILLEGAL_STATE(state));
        }
        changeState(State.CONNECTING);
        scheduleConnectTimeout();
        filterChain.connect(new InetSocketAddress(uri.getHost(), Utils.getPort(uri)), null);
    }

    synchronized void send(final HttpRequest httpRequest) {
        if (state != State.IDLE) {
            throw new IllegalStateException(
                    "Http request cannot be sent over a connection that is in other state than IDLE. Current state: " + state);
        }

        cancelIdleTimeout();

        this.httpRequest = httpRequest;
        // clean state left by previous request
        httResponse = null;
        error = null;
        persistentConnection = true;
        changeState(State.SENDING_REQUEST);

        addRequestHeaders();

        filterChain.write(httpRequest, new CompletionHandler<HttpRequest>() {
            @Override
            public void failed(Throwable throwable) {
                handleError(throwable);
            }

            @Override
            public void completed(HttpRequest result) {
                handleHeaderSent();
            }
        });
    }

    void close() {
        if (state == State.CLOSED) {
            return;
        }

        cancelAllTimeouts();
        filterChain.close();
        changeState(State.CLOSED);
    }

    private synchronized void handleHeaderSent() {
        if (state != State.SENDING_REQUEST) {
            return;
        }

        scheduleResponseTimeout();

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.NONE
                || httpRequest.getBodyMode() == HttpRequest.BodyMode.BUFFERED) {
            changeState(State.RECEIVING_HEADER);
        } else {
            ChunkedBodyOutputStream bodyStream = (ChunkedBodyOutputStream) httpRequest.getBodyStream();
            bodyStream.setCloseListener(() -> {
                synchronized (HttpConnection.this) {
                    if (state != State.SENDING_REQUEST) {
                        return;
                    }
                }
                changeState(State.RECEIVING_HEADER);
            });
        }
    }

    private void addRequestHeaders() {
        Map<String, List<String>> cookies;
        try {
            cookies = cookieManager.get(httpRequest.getUri(), httpRequest.getHeaders());
        } catch (IOException e) {
            handleError(e);
            return;
        }

        // unfortunately CookieManager returns ""Cookie" -> empty list" pair if the cookie is not set
        cookies.entrySet().stream().filter(cookieHeader -> cookieHeader.getValue() != null && !cookieHeader.getValue().isEmpty())
                .forEach(cookieHeader -> httpRequest.getHeaders().put(cookieHeader.getKey(), cookieHeader.getValue()));
    }

    private void processResponseHeaders(HttpResponse response) throws IOException {
        cookieManager.put(httpRequest.getUri(), httResponse.getHeaders());
        List<String> connectionValues = response.getHeader(Constants.CONNECTION);
        if (connectionValues != null) {
            connectionValues.stream().filter(connectionValue -> connectionValue.equalsIgnoreCase(Constants.CONNECTION_CLOSE))
                    .forEach(connectionValue -> persistentConnection = false);
        }
    }

    protected Filter<HttpRequest, HttpResponse, HttpRequest, HttpResponse> createFilterChain(URI uri,
                                                                                             ConnectorConfiguration
                                                                                                     configuration) {
        boolean secure = Constants.HTTPS.equals(uri.getScheme());
        Filter<ByteBuffer, ByteBuffer, ?, ?> socket;

        if (secure) {
            SSLContext sslContext = configuration.getSslContext();
            TransportFilter transportFilter = new TransportFilter(SSL_INPUT_BUFFER_SIZE, configuration.getThreadPoolConfig(),
                    configuration.getContainerIdleTimeout());

            if (sslContext == null) {
                sslContext = SslConfigurator.getDefaultContext();

            }

            socket = new SslFilter(transportFilter, sslContext, uri.getHost(), configuration.getHostnameVerifier());
        } else {
            socket = new TransportFilter(INPUT_BUFFER_SIZE, configuration.getThreadPoolConfig(),
                    configuration.getContainerIdleTimeout());
        }

        int maxHeaderSize = configuration.getMaxHeaderSize();
        HttpFilter httpFilter = new HttpFilter(socket, maxHeaderSize, maxHeaderSize + INPUT_BUFFER_SIZE);

        ConnectorConfiguration.ProxyConfiguration proxyConfiguration = configuration.getProxyConfiguration();
        if (proxyConfiguration.isConfigured()) {
            ProxyFilter proxyFilter = new ProxyFilter(httpFilter, proxyConfiguration);
            return new ConnectionFilter(proxyFilter);
        }

        return new ConnectionFilter(httpFilter);
    }

    private void changeState(State newState) {
        if (state == State.CLOSED) {
            return;
        }
        State old = state;
        state = newState;

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(LocalizationMessages.CONNECTION_CHANGING_STATE(uri.getHost(), uri.getPort(), old, newState));
        }

        stateListener.onStateChanged(this, old, newState);
    }

    private void scheduleResponseTimeout() {
        if (configuration.getResponseTimeout() == 0) {
            return;
        }

        responseTimeout = scheduler.schedule(() -> {
            synchronized (HttpConnection.this) {
                if (state != State.RECEIVING_HEADER && state != State.RECEIVING_BODY) {
                    return;
                }

                responseTimeout = null;
                changeState(State.RESPONSE_TIMEOUT);
                close();
            }
        }, configuration.getResponseTimeout(), TimeUnit.MILLISECONDS);
    }

    private void cancelResponseTimeout() {
        if (responseTimeout != null) {
            responseTimeout.cancel(true);
            responseTimeout = null;
        }
    }

    private void scheduleConnectTimeout() {
        if (configuration.getConnectTimeout() == 0) {
            return;
        }

        connectTimeout = scheduler.schedule(() -> {
            synchronized (HttpConnection.this) {
                if (state != State.CONNECTING) {
                    return;
                }

                connectTimeout = null;
                changeState(State.CONNECT_TIMEOUT);
                close();
            }
        }, configuration.getConnectTimeout(), TimeUnit.MILLISECONDS);
    }

    private void cancelConnectTimeout() {
        if (connectTimeout != null) {
            connectTimeout.cancel(true);
            connectTimeout = null;
        }
    }

    private void scheduleIdleTimeout() {
        if (configuration.getConnectionIdleTimeout() == 0) {
            return;
        }

        idleTimeout = scheduler.schedule(() -> {
            synchronized (HttpConnection.this) {
                if (state != State.IDLE) {
                    return;
                }
                idleTimeout = null;
                changeState(State.IDLE_TIMEOUT);
                close();
            }
        }, configuration.getConnectionIdleTimeout(), TimeUnit.MILLISECONDS);
    }

    private void cancelIdleTimeout() {
        if (idleTimeout != null) {
            idleTimeout.cancel(true);
            idleTimeout = null;
        }
    }

    private void cancelAllTimeouts() {
        cancelConnectTimeout();
        cancelIdleTimeout();
        cancelResponseTimeout();
    }

    private synchronized void handleError(Throwable t) {
        cancelAllTimeouts();
        error = t;
        changeState(State.ERROR);
        close();
    }

    private void changeStateToIdle() {
        scheduleIdleTimeout();
        changeState(State.IDLE);
    }

    Throwable getError() {
        return error;
    }

    HttpResponse getHttResponse() {
        return httResponse;
    }

    private synchronized void handleResponseRead() {
        cancelResponseTimeout();
        changeState(State.RECEIVED);
        if (!persistentConnection) {
            changeState(State.CLOSED);
            return;
        }
        changeStateToIdle();
    }

    private class ConnectionFilter extends Filter<HttpRequest, HttpResponse, HttpRequest, HttpResponse> {

        ConnectionFilter(Filter<HttpRequest, HttpResponse, ?, ?> downstreamFilter) {
            super(downstreamFilter);
        }

        @Override
        boolean processRead(HttpResponse response) {
            synchronized (HttpConnection.this) {
                if (state != State.RECEIVING_HEADER && state != State.SENDING_REQUEST) {
                    return false;
                }

                if (state == State.SENDING_REQUEST) {
                    // great we received response header so fast that we did not even switch into "receiving header" state,
                    // do it now to complete the formal lifecycle
                    // this happens when write completion listener is overtaken by "read event"
                    changeState(State.RECEIVING_HEADER);
                }

                httResponse = response;

                try {
                    processResponseHeaders(response);
                } catch (IOException e) {
                    handleError(e);
                    return false;
                }
            }

            if (response.getHasContent()) {
                AsynchronousBodyInputStream bodyStream = httResponse.getBodyStream();
                changeState(State.RECEIVING_BODY);
                bodyStream.setStateChangeLister(new AsynchronousBodyInputStream.StateChangeLister() {
                    @Override
                    public void onError(Throwable t) {
                        handleError(t);
                    }

                    @Override
                    public void onAllDataRead() {
                        handleResponseRead();
                    }
                });

            } else {
                handleResponseRead();
            }
            return false;
        }

        @Override
        void processConnect() {
            synchronized (HttpConnection.this) {
                if (state != State.CONNECTING) {
                    return;
                }

                downstreamFilter.startSsl();
            }
        }

        @Override
        void processSslHandshakeCompleted() {
            synchronized (HttpConnection.this) {
                if (state != State.CONNECTING) {
                    return;
                }

                cancelConnectTimeout();
                changeStateToIdle();
            }
        }

        @Override
        void processConnectionClosed() {
            synchronized (HttpConnection.this) {
                cancelAllTimeouts();
                changeState(State.CLOSED_BY_SERVER);
                HttpConnection.this.close();
            }
        }

        @Override
        void processError(Throwable t) {
            handleError(t);
        }

        @Override
        void write(HttpRequest data, CompletionHandler<HttpRequest> completionHandler) {
            downstreamFilter.write(data, completionHandler);
        }
    }

    enum State {
        CREATED,
        CONNECTING,
        CONNECT_TIMEOUT,
        IDLE,
        SENDING_REQUEST,
        RECEIVING_HEADER,
        RECEIVING_BODY,
        RECEIVED,
        RESPONSE_TIMEOUT,
        CLOSED_BY_SERVER,
        CLOSED,
        ERROR,
        IDLE_TIMEOUT
    }

    interface StateChangeListener {

        void onStateChanged(HttpConnection connection, State oldState, State newState);
    }
}
