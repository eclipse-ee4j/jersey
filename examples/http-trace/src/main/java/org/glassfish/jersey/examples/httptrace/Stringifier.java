/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httptrace;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Request;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * Request stringifier.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Stringifier {

    private Stringifier() {
    }

    public static String stringify(ContainerRequest request) {
        StringBuilder buffer = new StringBuilder();

        printRequestLine(buffer, request);
        printPrefixedHeaders(buffer, request.getHeaders());

        if (request.hasEntity()) {
            buffer.append(request.readEntity(String.class)).append("\n");
        }

        return buffer.toString();
    }

    private static void printRequestLine(StringBuilder buffer, ContainerRequest request) {
        buffer.append(request.getMethod()).append(" ").append(request.getUriInfo().getRequestUri().toASCIIString()).append("\n");
    }

    private static void printPrefixedHeaders(StringBuilder buffer, Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            List<String> val = e.getValue();
            String header = e.getKey();

            if (val.size() == 1) {
                buffer.append(header).append(": ").append(val.get(0)).append("\n");
            } else {
                StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (String s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(s);
                }
                buffer.append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }
}
