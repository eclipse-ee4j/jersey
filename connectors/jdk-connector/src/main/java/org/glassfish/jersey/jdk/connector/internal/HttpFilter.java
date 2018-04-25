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

import java.net.URI;
import java.nio.ByteBuffer;

import javax.ws.rs.core.HttpHeaders;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpFilter extends Filter<HttpRequest, HttpResponse, ByteBuffer, ByteBuffer> {

    private final HttpParser httpParser;

    /**
     * Constructor.
     *
     * @param downstreamFilter downstream filter. Accessible directly as {@link #downstreamFilter} protected field.
     */
    HttpFilter(Filter<ByteBuffer, ByteBuffer, ?, ?> downstreamFilter, int maxHeaderSize, int maxBufferSize) {
        super(downstreamFilter);
        this.httpParser = new HttpParser(maxHeaderSize, maxBufferSize);
    }

    @Override
    void write(final HttpRequest httpRequest, final CompletionHandler<HttpRequest> completionHandler) {
        addTransportHeaders(httpRequest);

        ByteBuffer header = HttpRequestEncoder.encodeHeader(httpRequest);
        prepareForReply(httpRequest, completionHandler);
        downstreamFilter.write(header, new CompletionHandler<ByteBuffer>() {
            @Override
            public void failed(Throwable throwable) {
                completionHandler.failed(throwable);
            }

            @Override
            public void completed(ByteBuffer result) {
                writeBody(httpRequest, completionHandler);
            }
        });
    }

    private void writeBody(final HttpRequest httpRequest, final CompletionHandler<HttpRequest> completionHandler) {
        switch (httpRequest.getBodyMode()) {

            case CHUNKED: {
                ChunkedBodyOutputStream bodyStream = (ChunkedBodyOutputStream) httpRequest.getBodyStream();
                bodyStream.open(downstreamFilter);
                break;
            }

            case BUFFERED: {
                ByteBuffer body = httpRequest.getBufferedBody();
                downstreamFilter.write(body, new CompletionHandler<ByteBuffer>() {
                    @Override
                    public void failed(Throwable throwable) {
                        completionHandler.failed(throwable);
                    }
                });

                break;
            }
        }
    }

    private void prepareForReply(HttpRequest httpRequest, CompletionHandler<HttpRequest> completionHandler) {
        completionHandler.completed(httpRequest);

        boolean expectResponseBody = true;

        if (Constants.HEAD.equals(httpRequest.getMethod()) || Constants.CONNECT.equals(httpRequest.getMethod())) {
            expectResponseBody = false;
        }

        httpParser.reset(expectResponseBody);
    }

    @Override
    boolean processRead(ByteBuffer data) {
        boolean headerParsed = httpParser.isHeaderParsed();
        try {
            httpParser.parse(data);
        } catch (ParseException e) {
            onError(e);
        }

        if (!headerParsed && httpParser.isHeaderParsed()) {
            HttpResponse httpResponse = httpParser.getHttpResponse();
            upstreamFilter.onRead(httpResponse);
        }

        return false;
    }

    private void addTransportHeaders(HttpRequest httpRequest) {
        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.BUFFERED) {
            httpRequest.addHeaderIfNotPresent(Constants.CONTENT_LENGTH, Integer.toString(httpRequest.getBodySize()));
        }

        URI uri = httpRequest.getUri();
        int port = Utils.getPort(uri);
        httpRequest.addHeaderIfNotPresent(Constants.HOST, uri.getHost() + ":" + port);

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.CHUNKED) {
            httpRequest.addHeaderIfNotPresent(Constants.TRANSFER_ENCODING_HEADER, Constants.TRANSFER_ENCODING_CHUNKED);
        }

        if (httpRequest.getBodyMode() == HttpRequest.BodyMode.NONE) {
            httpRequest.addHeaderIfNotPresent(HttpHeaders.CONTENT_LENGTH, Integer.toString(0));
        }
    }
}
