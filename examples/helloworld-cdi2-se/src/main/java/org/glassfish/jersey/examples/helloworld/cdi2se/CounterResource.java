/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.inject.Inject;

/**
 * Request-scoped resource counter.
 *
 * @author Petr Bouda
 */
@Path("counter")
public class CounterResource {

    @Inject
    private RequestScopedCounter requestScoped;

    @Inject
    private ApplicationScopedCounter applicationScoped;

    @GET
    @Path("application")
    @Produces("text/plain")
    public int getAppCounter() {
        return applicationScoped.getNumber();
    }

    @GET
    @Path("request")
    @Produces("text/plain")
    public int getReqCounter() {
        return requestScoped.getNumber();
    }
}
