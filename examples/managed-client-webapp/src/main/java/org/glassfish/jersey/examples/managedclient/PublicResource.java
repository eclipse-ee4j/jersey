/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.Uri;

/**
 * A resource that uses managed clients to retrieve values of internal
 * resources 'A' and 'B', which are protected by a {@link CustomHeaderFilter}
 * and require a specific custom header in a request to be set to a specific value.
 * <p>
 * Properly configured managed clients have a {@code CustomHeaderFilter} instance
 * configured to insert the {@link CustomHeaderFeature.Require required} custom header
 * with a proper value into the outgoing client requests.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("public")
public class PublicResource {

    @Uri("a") @ClientA // resolves to <base>/internal/a
    private WebTarget targetA;

    @GET
    @Produces("text/plain")
    @Path("a")
    public String getTargetA() {
        return targetA.request(MediaType.TEXT_PLAIN).get(String.class);
    }

    @GET
    @Produces("text/plain")
    @Path("b")
    public Response getTargetB(@Uri("internal/b") @ClientB WebTarget targetB) {
        return targetB.request(MediaType.TEXT_PLAIN).get();
    }
}
