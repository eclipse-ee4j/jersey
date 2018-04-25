/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httpsclientservergrizzly;

import java.nio.charset.Charset;
import javax.xml.bind.DatatypeConverter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 * Simple resource demonstrating low level approach of getting user credentials.
 *
 * Better way would be injecting {@link javax.ws.rs.core.SecurityContext}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Path("/")
public class RootResource {

    @GET
    public String get1(@Context HttpHeaders headers) {
        // you can get username form HttpHeaders
        System.out.println("Service: GET / User: " + getUser(headers));

        return Server.CONTENT;
    }

    private String getUser(HttpHeaders headers) {

        // this is a very minimalistic and "naive" code; if you plan to use it
        // add necessary checks (see org.glassfish.jersey.examples.httpsclientservergrizzly.authservergrizzly.SecurityFilter)

        String auth = headers.getRequestHeader("authorization").get(0);

        auth = auth.substring("Basic ".length());
        String[] values = new String(DatatypeConverter.parseBase64Binary(auth), Charset.forName("ASCII")).split(":");

        // String username = values[0];
        // String password = values[1];

        return values[0];
    }
}
