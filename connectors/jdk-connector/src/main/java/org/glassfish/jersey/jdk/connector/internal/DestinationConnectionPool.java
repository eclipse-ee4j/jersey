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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class DestinationConnectionPool {

    private final ConnectorConfiguration configuration;
    private final Queue<HttpConnection> idleConnections = new ConcurrentLinkedDeque<>();
    private final Set<HttpConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Queue<RequestRecord> pendingRequests = new ConcurrentLinkedDeque<>();
    private final Map<HttpConnection, RequestRecord> requestsInProgress = new HashMap<>();
    private final CookieManager cookieManager;
    private final ScheduledExecutorService scheduler;
    private final ConnectionStateListener connectionStateListener;

    private volatile ConnectionCloseListener connectionCloseListener;

    private int connectionCounter = 0;
    private boolean closed = false;

    DestinationConnectionPool(ConnectorConfiguration configuration,
                              CookieManager cookieManager,
                              ScheduledExecutorService scheduler) {
        this.configuration = configuration;
        this.cookieManager = cookieManager;
        this.scheduler = scheduler;
        this.connectionStateListener = new ConnectionStateListener();
    }

    void setConnectionCloseListener(ConnectionCloseListener connectionCloseListener) {
        this.connectionCloseListener = connectionCloseListener;
    }

    void send(HttpRequest httpRequest, CompletionHandler<HttpResponse> completionHandler) {
        pendingRequests.add(new RequestRecord(httpRequest, completionHandler));
        processPendingRequests();
    }

    private void processPendingRequests(HttpConnection connection) {
        HttpRequest httpRequest;
        CompletionHandler<HttpResponse> completionHandler;

        synchronized (this) {
        /* this is synchronized so that another thread does not steal the pending request at the head of the queue
           while we investigate if we can execute it. */
            RequestRecord pendingHead = pendingRequests.poll();
            if (pendingHead == null) {

                idleConnections.add(connection);

                // no pending requests
                return;
            }

            httpRequest = pendingHead.request;
            completionHandler = pendingHead.completionHandler;
        }

        // if there was a connection available just use it
        requestsInProgress.put(connection, new RequestRecord(httpRequest, completionHandler));
        connection.send(httpRequest);
    }

    private void processPendingRequests() {
        HttpConnection connection;
        HttpRequest httpRequest;
        CompletionHandler<HttpResponse> completionHandler;

        synchronized (this) {
            /* this is synchronized so that another thread does not steal the pending request at the head of the queue
            while we investigate if we can execute it. */
            RequestRecord pendingHead = pendingRequests.peek();
            if (pendingHead == null) {
                // no pending requests
                return;
            }

            httpRequest = pendingHead.request;
            completionHandler = pendingHead.completionHandler;

            connection = idleConnections.poll();
            if (connection != null) {
                pendingRequests.poll();
            }
        }

        if (connection != null) {
            // if there was a connection available just use it
            requestsInProgress.put(connection, new RequestRecord(httpRequest, completionHandler));
            connection.send(httpRequest);
            return;
        }

        // if there was not a connection available keep this requests in pending list and try to create a connection
        synchronized (this) {
            // synchronized because other thread might open/close connections, so we have to make sure we get the limits right.

            if (configuration.getMaxConnectionsPerDestination() == connectionCounter) {
                // we are at the limit for this destination, just wait for a connection to become idle or close
                return;
            }

            // create a connection
            connection = new HttpConnection(httpRequest.getUri(), cookieManager, configuration, scheduler,
                    connectionStateListener);
            connections.add(connection);
            connectionCounter++;
        }

        // we don't want to connect inside the synchronized block
        connection.connect();
    }

    synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;

        connections.forEach(HttpConnection::close);
    }

    private RequestRecord getRequest(HttpConnection connection) {
        RequestRecord requestRecord = requestsInProgress.get(connection);
        if (requestRecord == null) {
            throw new IllegalStateException("Request not found");
        }

        return requestRecord;
    }

    private RequestRecord removeRequest(HttpConnection connection) {
        RequestRecord requestRecord = requestsInProgress.get(connection);
        if (requestRecord == null) {
            throw new IllegalStateException("Request not found");
        }

        return requestRecord;
    }

    private void cleanClosedConnection(HttpConnection connection) {
        if (closed) {
            return;
        }

        RequestRecord pendingRequest;
        synchronized (this) {
            idleConnections.remove(connection);
            connections.remove(connection);
            connectionCounter--;

            pendingRequest = pendingRequests.peek();
            if (pendingRequest == null) {
                if (connectionCounter == 0) {
                    connectionCloseListener.onLastConnectionClosed();
                }
                return;
            }
        }

        processPendingRequests();
    }

    private void handleIllegalStateTransition(HttpConnection.State oldState, HttpConnection.State newState) {
        throw new IllegalStateException("Illegal state transition, old state: " + oldState + " new state: " + newState);
    }

    private synchronized void removeAllPendingWithError(Throwable t) {
        for (RequestRecord requestRecord : pendingRequests) {
            requestRecord.completionHandler.failed(t);
        }

        pendingRequests.clear();
    }

    private class ConnectionStateListener implements HttpConnection.StateChangeListener {

        @Override
        public void onStateChanged(HttpConnection connection, HttpConnection.State oldState, HttpConnection.State newState) {
            switch (newState) {

                case IDLE: {
                    switch (oldState) {
                        case RECEIVED:
                        case CONNECTING: {
                            processPendingRequests(connection);
                            return;
                        }

                        default: {
                            handleIllegalStateTransition(oldState, newState);
                            return;
                        }
                    }
                }

                case RECEIVED: {
                    switch (oldState) {
                        case RECEIVING_HEADER: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler.completed(connection.getHttResponse());
                            return;
                        }

                        case RECEIVING_BODY: {
                            removeRequest(connection);
                            return;
                        }

                        default: {
                            handleIllegalStateTransition(oldState, newState);
                            return;
                        }
                    }
                }

                case RECEIVING_BODY: {
                    switch (oldState) {
                        case RECEIVING_HEADER: {
                            RequestRecord request = getRequest(connection);
                            request.response = connection.getHttResponse();
                            request.completionHandler.completed(connection.getHttResponse());
                            return;
                        }

                        default: {
                            handleIllegalStateTransition(oldState, newState);
                            return;
                        }
                    }
                }

                case ERROR: {
                    switch (oldState) {
                        case SENDING_REQUEST: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler.failed(connection.getError());
                            return;
                        }

                        case RECEIVING_HEADER: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler.failed(connection.getError());
                            return;
                        }

                        case RECEIVING_BODY: {
                            requestsInProgress.remove(connection);
                            return;
                        }

                        case CONNECTING: {
                            removeAllPendingWithError(connection.getError());
                            return;
                        }

                        default: {
                            connection.getError().printStackTrace();
                            handleIllegalStateTransition(oldState, newState);
                            return;
                        }
                    }
                }

                case RESPONSE_TIMEOUT: {
                    switch (oldState) {
                        case RECEIVING_HEADER: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler
                                    .failed(new IOException(LocalizationMessages.TIMEOUT_RECEIVING_RESPONSE()));
                            return;
                        }

                        case RECEIVING_BODY: {
                            RequestRecord request = requestsInProgress.remove(connection);
                            request.response.getBodyStream()
                                    .notifyError(new IOException(LocalizationMessages.TIMEOUT_RECEIVING_RESPONSE_BODY()));
                            return;
                        }

                        default: {
                            handleIllegalStateTransition(oldState, newState);
                            return;
                        }
                    }
                }

                case CLOSED_BY_SERVER: {
                    switch (oldState) {
                        case SENDING_REQUEST: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler
                                    .failed(new IOException(LocalizationMessages.CLOSED_WHILE_SENDING_REQUEST()));
                            return;
                        }

                        case RECEIVING_HEADER: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler
                                    .failed(new IOException(LocalizationMessages.CLOSED_WHILE_RECEIVING_RESPONSE(),
                                            connection.getError()));
                            return;
                        }

                        case RECEIVING_BODY: {
                            RequestRecord request = requestsInProgress.remove(connection);
                            request.response.getBodyStream().notifyError(
                                    new IOException(LocalizationMessages.CLOSED_WHILE_RECEIVING_BODY(),
                                            connection.getError()));
                            return;
                        }

                        case CONNECTING: {
                            removeAllPendingWithError(new IOException(LocalizationMessages.CONNECTION_CLOSED()));
                            return;
                        }
                    }
                }

                case CLOSED: {
                    switch (oldState) {
                        case SENDING_REQUEST: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler
                                    .failed(new IOException(LocalizationMessages.CLOSED_BY_CLIENT_WHILE_SENDING()));
                            cleanClosedConnection(connection);
                            return;
                        }

                        case RECEIVING_HEADER: {
                            RequestRecord request = removeRequest(connection);
                            request.completionHandler
                                    .failed(new IOException(LocalizationMessages.CLOSED_WHILE_RECEIVING_RESPONSE()));
                            cleanClosedConnection(connection);
                            return;
                        }

                        case RECEIVING_BODY: {
                            RequestRecord request = requestsInProgress.remove(connection);
                            request.response.getBodyStream().notifyError(
                                    new IOException(LocalizationMessages.CLOSED_BY_CLIENT_WHILE_RECEIVING_BODY(),
                                            connection.getError()));
                            cleanClosedConnection(connection);
                            return;
                        }

                        default: {
                            cleanClosedConnection(connection);
                            return;
                        }
                    }
                }

                case CONNECT_TIMEOUT: {
                    switch (oldState) {
                        case CONNECTING: {
                            removeAllPendingWithError(new IOException(LocalizationMessages.CONNECTION_TIMEOUT()));
                            return;
                        }

                        default: {
                            cleanClosedConnection(connection);
                        }
                    }
                }
            }
        }
    }

    private static class RequestRecord {

        private final HttpRequest request;
        private final CompletionHandler<HttpResponse> completionHandler;
        private HttpResponse response;

        RequestRecord(HttpRequest request, CompletionHandler<HttpResponse> completionHandler) {
            this.request = request;
            this.completionHandler = completionHandler;
        }
    }

    static class DestinationKey {

        private final String host;
        private final int port;
        private final boolean secure;

        DestinationKey(URI uri) {
            host = uri.getHost();
            port = Utils.getPort(uri);
            secure = Constants.HTTPS.equalsIgnoreCase(uri.getScheme());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DestinationKey that = (DestinationKey) o;

            return port == that.port && secure == that.secure && host.equals(that.host);
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + port;
            result = 31 * result + (secure ? 1 : 0);
            return result;
        }
    }

    interface ConnectionCloseListener {

        void onLastConnectionClosed();
    }
}
