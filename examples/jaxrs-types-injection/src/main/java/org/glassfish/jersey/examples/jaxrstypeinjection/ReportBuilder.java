/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxrstypeinjection;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * Provides functionality for appending values of JAX-RS types to a string-based
 * report.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class ReportBuilder {

    private ReportBuilder() {
    }

    public static StringBuilder append(StringBuilder sb, UriInfo uriInfo, HttpHeaders httpHeaders) {
        sb.append("\n UriInfo:");
        sb.append("\n   Absolute path : ").append(uriInfo.getAbsolutePath());
        sb.append("\n   Base URI : ").append(uriInfo.getBaseUri());
        sb.append("\n   Mathced resources : ").append(uriInfo.getMatchedResources().toString());
        sb.append("\n   Matched URIs : ").append(uriInfo.getMatchedURIs().toString());
        sb.append("\n   Path : ").append(uriInfo.getPath());
        sb.append("\n   Path parameters:\n");
        dumpMultivaluedMap(sb, uriInfo.getPathParameters());
        sb.append("   Path segments : ").append(uriInfo.getPathSegments().toString());
        sb.append("\n   Query parameters:\n");
        dumpMultivaluedMap(sb, uriInfo.getQueryParameters());
        sb.append("   Request URI : ").append(uriInfo.getRequestUri());
        sb.append("\n\n HttpHeaders:\n");
        dumpMultivaluedMap(sb, httpHeaders.getRequestHeaders());
        return sb;
    }

    public static void dumpMultivaluedMap(StringBuilder sb, MultivaluedMap<String, String> map) {
        if (map == null) {
            sb.append("     [ null ]\n");
            return;
        }
        for (Map.Entry<String, List<String>> headerEntry : map.entrySet()) {
            sb.append("     ").append(headerEntry.getKey()).append(" : ");
            final Iterator<String> valueIterator = headerEntry.getValue().iterator();
            sb.append(valueIterator.next());
            while (valueIterator.hasNext()) {
                sb.append(", ").append(valueIterator.next());
            }
            sb.append('\n');
        }
    }
}
