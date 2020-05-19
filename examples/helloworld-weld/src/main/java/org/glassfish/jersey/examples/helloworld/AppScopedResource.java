/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * Application scoped CDI based resource.
 *
 * @author Jakub Podlesak
 */
@Path("app")
@ApplicationScoped
public class AppScopedResource {

    AtomicInteger counter = new AtomicInteger();

    @GET
    @Path("count")
    @Produces("text/plain")
    public int getCount() {
        return counter.incrementAndGet();
    }
}
