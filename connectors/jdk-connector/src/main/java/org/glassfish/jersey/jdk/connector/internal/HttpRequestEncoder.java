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
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpRequestEncoder {

    private static final String ENCODING = "ISO-8859-1";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final byte[] LINE_SEPARATOR_BYTES = LINE_SEPARATOR.getBytes(Charset.forName(ENCODING));
    private static final byte[] LAST_CHUNK = "0\r\n\r\n".getBytes(Charset.forName(ENCODING));
    private static final String HTTP_VERSION = "HTTP/1.1";

    private static void appendUpgradeHeaders(StringBuilder request, Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            StringBuilder value = new StringBuilder();
            for (String valuePart : header.getValue()) {
                if (value.length() != 0) {
                    value.append(",");
                }
                value.append(valuePart);
            }
            appendHeader(request, header.getKey(), value.toString());
        }

        request.append(LINE_SEPARATOR);
    }

    private static void appendHeader(StringBuilder request, String key, String value) {
        request.append(key);
        request.append(": ");
        request.append(value);
        request.append(LINE_SEPARATOR);
    }

    private static void appendFirstLine(StringBuilder request, HttpRequest httpRequest) {
        request.append(httpRequest.getMethod());
        request.append(" ");
        if (httpRequest.getMethod().equals(Constants.CONNECT)) {
            request.append(httpRequest.getUri().toString());
        } else {
            URI uri = httpRequest.getUri();
            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            if (uri.getRawQuery() != null) {
                path += "?" + uri.getRawQuery();
            }

            request.append(path);
        }
        request.append(" ");
        request.append(HTTP_VERSION);
        request.append(LINE_SEPARATOR);
    }

    static ByteBuffer encodeHeader(HttpRequest httpRequest) {
        StringBuilder request = new StringBuilder();
        appendFirstLine(request, httpRequest);
        appendUpgradeHeaders(request, httpRequest.getHeaders());
        String requestStr = request.toString();
        byte[] bytes = requestStr.getBytes(Charset.forName(ENCODING));
        return ByteBuffer.wrap(bytes);
    }

    static ByteBuffer encodeChunk(ByteBuffer data) {
        if (data.remaining() == 0) {
            return ByteBuffer.wrap(LAST_CHUNK);
        }

        byte[] startBytes = getChunkHeaderBytes(data.remaining());
        ByteBuffer chunkBuffer = ByteBuffer.allocate(startBytes.length + data.remaining() + 2);
        chunkBuffer.put(startBytes);
        chunkBuffer.put(data);
        chunkBuffer.put(LINE_SEPARATOR_BYTES);
        chunkBuffer.flip();

        return chunkBuffer;
    }

    private static byte[] getChunkHeaderBytes(int dataLength) {
        String chunkStart = Integer.toHexString(dataLength) + LINE_SEPARATOR;
        return chunkStart.getBytes(Charset.forName(ENCODING));
    }

    static int getChunkSize(int dataLength) {
        if (dataLength == 0) {
            return LAST_CHUNK.length;
        }

        return getChunkHeaderBytes(dataLength).length + dataLength + 2;
    }
}
