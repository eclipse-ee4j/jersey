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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpRequest {

    private final String method;
    private final URI uri;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final BodyMode bodyMode;
    private final BodyOutputStream bodyStream;

    private HttpRequest(String method, URI uri, BodyMode bodyMode, BodyOutputStream bodyStream) {
        this.method = method;
        this.uri = uri;
        this.bodyMode = bodyMode;
        this.bodyStream = bodyStream;
    }

    static HttpRequest createBodyless(String method, URI uri) {
        HttpRequest httpRequest = new HttpRequest(method, uri, BodyMode.NONE, null);
        return httpRequest;
    }

    static HttpRequest createChunked(String method, URI uri, int chunkSize) {
        ChunkedBodyOutputStream bodyStream = new ChunkedBodyOutputStream(chunkSize);
        return new HttpRequest(method, uri, BodyMode.CHUNKED, bodyStream);
    }

    static HttpRequest createBuffered(String method, URI uri) {
        BufferedBodyOutputStream bodyOutputStream = new BufferedBodyOutputStream();
        return new HttpRequest(method, uri, BodyMode.BUFFERED, bodyOutputStream);
    }

    String getMethod() {
        return method;
    }

    URI getUri() {
        return uri;
    }

    Map<String, List<String>> getHeaders() {
        return headers;
    }

    BodyMode getBodyMode() {
        return bodyMode;
    }

    BodyOutputStream getBodyStream() {
        if (BodyMode.NONE == bodyMode) {
            throw new IllegalStateException(LocalizationMessages.HTTP_REQUEST_NO_BODY());
        }

        return bodyStream;
    }

    void addHeaderIfNotPresent(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<>(1);
            headers.put(name, values);
            values.add(value);
        }
    }

    ByteBuffer getBufferedBody() {
        if (BodyMode.BUFFERED != bodyMode) {
            throw new IllegalStateException(LocalizationMessages.HTTP_REQUEST_NO_BUFFERED_BODY());
        }

        return ((BufferedBodyOutputStream) bodyStream).toBuffer();
    }

    int getBodySize() {
        if (bodyMode == BodyMode.CHUNKED) {
            throw new IllegalStateException(LocalizationMessages.HTTP_REQUEST_BODY_SIZE_NOT_AVAILABLE());
        }

        return ((BufferedBodyOutputStream) bodyStream).toBuffer().remaining();
    }

    enum BodyMode {
        NONE,
        CHUNKED,
        BUFFERED
    }
}
