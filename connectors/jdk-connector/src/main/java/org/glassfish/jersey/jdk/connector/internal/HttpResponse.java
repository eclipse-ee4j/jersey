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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpResponse {

    private final String protocolVersion;
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final Map<String, List<String>> trailerHeaders = new HashMap<>(0);
    private final AsynchronousBodyInputStream bodyStream;
    private volatile boolean hasContent = true;

    HttpResponse(String protocolVersion, int statusCode, String reasonPhrase) {
        this.protocolVersion = protocolVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        bodyStream = new AsynchronousBodyInputStream();
    }

    String getProtocolVersion() {
        return protocolVersion;
    }

    int getStatusCode() {
        return statusCode;
    }

    String getReasonPhrase() {
        return reasonPhrase;
    }

    void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    boolean getHasContent() {
        return hasContent;
    }

    Map<String, List<String>> getHeaders() {
        return headers;
    }

    List<String> getHeader(String name) {
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return headers.get(headerName);
            }
        }

        return null;
    }

    void addHeader(String name, String value) {
        List<String> values = getHeader(name);
        if (values == null) {
            values = new ArrayList<>(1);
            headers.put(name, values);
        }

        values.add(value);
    }

    List<String> getTrailerHeader(String name) {
        for (String headerName : trailerHeaders.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return trailerHeaders.get(headerName);
            }
        }

        return null;
    }

    void addTrailerHeader(String name, String value) {
        List<String> values = getTrailerHeader(name);
        if (values == null) {
            values = new ArrayList<>(1);
            trailerHeaders.put(name, values);
        }

        values.add(value);
    }

    AsynchronousBodyInputStream getBodyStream() {
        return bodyStream;
    }
}
