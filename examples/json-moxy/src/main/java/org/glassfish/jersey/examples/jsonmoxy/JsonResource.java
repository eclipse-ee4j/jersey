/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonmoxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Pavel Bucek
 */
@Path("test")
public class JsonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TestBean createSimpleBean() {
        return new TestBean("a", 1, 1L);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TestBean roundTrip(TestBean s) {
        return s;
    }
}
