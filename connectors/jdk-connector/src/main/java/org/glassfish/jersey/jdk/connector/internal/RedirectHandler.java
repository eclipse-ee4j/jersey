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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
 */
class RedirectHandler {

    private static final Set<Integer> REDIRECT_STATUS_CODES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(301, 302, 303, 307, 308)));

    private final int maxRedirects;
    private final boolean followRedirects;
    private final Set<URI> redirectUriHistory;
    private final HttpConnectionPool httpConnectionPool;
    private final HttpRequest originalHttpRequest;

    private volatile URI lastRequestUri = null;

    RedirectHandler(HttpConnectionPool httpConnectionPool, HttpRequest originalHttpRequest,
                    ConnectorConfiguration connectorConfiguration) {
        this.followRedirects = connectorConfiguration.getFollowRedirects();
        this.maxRedirects = connectorConfiguration.getMaxRedirects();
        this.httpConnectionPool = httpConnectionPool;
        this.originalHttpRequest = originalHttpRequest;
        this.redirectUriHistory = new HashSet<>(maxRedirects);
        this.lastRequestUri = originalHttpRequest.getUri();
    }

    void handleRedirects(final HttpResponse httpResponse, final CompletionHandler<HttpResponse> completionHandler) {
        if (!followRedirects) {
            completionHandler.completed(httpResponse);
            return;
        }

        if (!REDIRECT_STATUS_CODES.contains(httpResponse.getStatusCode())) {
            completionHandler.completed(httpResponse);
            return;
        }

        if (httpResponse.getStatusCode() != 303) {
            // we support other methods than GET and HEAD only with 303
            if (!Constants.HEAD.equals(originalHttpRequest.getMethod()) && !Constants.GET
                    .equals(originalHttpRequest.getMethod())) {
                completionHandler.completed(httpResponse);
                return;
            }
        }

        // reading the body is not necessary, but if we wait until the entire body has arrived, we can reuse the same connection
        consumeBodyIfPresent(httpResponse, new CompletionHandler<Void>() {
            @Override
            public void failed(Throwable throwable) {
                completionHandler.failed(throwable);
            }

            @Override
            public void completed(Void r) {
                doRedirect(httpResponse, new CompletionHandler<HttpResponse>() {
                    @Override
                    public void failed(Throwable throwable) {
                        completionHandler.failed(throwable);
                    }

                    @Override
                    public void completed(HttpResponse result) {
                        handleRedirects(result, completionHandler);
                    }
                });
            }
        });
    }

    private void doRedirect(final HttpResponse httpResponse, final CompletionHandler<HttpResponse> completionHandler) {

        // get location header
        String locationString = null;
        final List<String> locationHeader = httpResponse.getHeader("Location");
        if (locationHeader != null && !locationHeader.isEmpty()) {
            locationString = locationHeader.get(0);
        }

        if (locationString == null || locationString.isEmpty()) {
            completionHandler.failed(new RedirectException(LocalizationMessages.REDIRECT_NO_LOCATION()));
            return;
        }

        URI location;
        try {
            location = new URI(locationString);

            if (!location.isAbsolute()) {
                // location is not absolute, we need to resolve it.
                URI baseUri = lastRequestUri;
                location = baseUri.resolve(location.normalize());
            }
        } catch (URISyntaxException e) {
            completionHandler.failed(new RedirectException(LocalizationMessages.REDIRECT_ERROR_DETERMINING_LOCATION(), e));
            return;
        }

        // infinite loop detection
        boolean alreadyRequested = !redirectUriHistory.add(location);
        if (alreadyRequested) {
            completionHandler.failed(new RedirectException(LocalizationMessages.REDIRECT_INFINITE_LOOP()));
            return;
        }

        // maximal number of redirection
        if (redirectUriHistory.size() > maxRedirects) {
            completionHandler.failed(new RedirectException(LocalizationMessages.REDIRECT_LIMIT_REACHED(maxRedirects)));
            return;
        }

        String method = originalHttpRequest.getMethod();
        Map<String, List<String>> headers = originalHttpRequest.getHeaders();
        if (httpResponse.getStatusCode() == 303 && !method.equals(Constants.HEAD)) {
            // in case of 303 we rewrite every method except HEAD to GET
            method = Constants.GET;
            // remove entity-transport headers if present
            headers.remove(Constants.CONTENT_LENGTH);
            headers.remove(Constants.TRANSFER_ENCODING_HEADER);
        }

        HttpRequest httpRequest = HttpRequest.createBodyless(method, location);
        httpRequest.getHeaders().putAll(headers);
        lastRequestUri = location;

        httpConnectionPool.send(httpRequest, completionHandler);
    }

    private void consumeBodyIfPresent(HttpResponse response, final CompletionHandler<Void> completionHandler) {
        final AsynchronousBodyInputStream bodyStream = response.getBodyStream();
        bodyStream.setReadListener(new ReadListener() {
            @Override
            public void onDataAvailable() {
                while (bodyStream.isReady()) {
                    try {
                        bodyStream.read();
                    } catch (IOException e) {
                        completionHandler.failed(e);
                    }
                }
            }

            @Override
            public void onAllDataRead() {
                completionHandler.completed(null);
            }

            @Override
            public void onError(Throwable t) {
                completionHandler.failed(t);
            }
        });
    }

    URI getLastRequestUri() {
        return lastRequestUri;
    }
}
