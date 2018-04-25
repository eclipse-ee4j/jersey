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

/**
 * Internal resource accessed from the managed client resource.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("internal")
public class InternalResource {

    @GET
    @Path("a")
    @CustomHeaderFeature.Require(headerName = "custom-header", headerValue = "a")
    public String getA() {
        return "a";
    }

    @GET
    @Path("b")
    @CustomHeaderFeature.Require(headerName = "custom-header", headerValue = "b")
    public String getB() {
        return "b";
    }
}
