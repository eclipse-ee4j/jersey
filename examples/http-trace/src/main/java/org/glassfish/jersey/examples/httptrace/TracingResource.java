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

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Request;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * This very basic resource showcases support of a HTTP TRACE method,
 * not directly supported by JAX-RS API.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path(App.ROOT_PATH_ANNOTATED)
public class TracingResource {

    @TRACE
    @Produces("text/plain")
    public String trace(Request request) {
        return Stringifier.stringify((ContainerRequest) request);
    }
}
