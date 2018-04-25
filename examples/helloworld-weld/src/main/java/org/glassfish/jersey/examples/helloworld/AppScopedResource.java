/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Application scoped CDI based resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
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
