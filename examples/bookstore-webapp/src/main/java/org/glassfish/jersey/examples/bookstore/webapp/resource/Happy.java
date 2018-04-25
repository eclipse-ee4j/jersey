/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import javax.inject.Singleton;

import org.glassfish.jersey.server.mvc.Template;

/**
 * Generates a header display resource which is useful for displaying the
 * various headers browsers use for making test cases
 */
@Template
@Path("/happy")
@Singleton
public class Happy {

    @Context
    private HttpHeaders headers;

    public String getHeaders() {
        StringBuilder buf = new StringBuilder();
        for (String header : headers.getRequestHeaders().keySet()) {
            buf.append("<li>");
            buf.append(header);
            buf.append(" = ");
            buf.append(headers.getRequestHeader(header));
            buf.append("</li>\n");
        }
        return buf.toString();
    }
}
