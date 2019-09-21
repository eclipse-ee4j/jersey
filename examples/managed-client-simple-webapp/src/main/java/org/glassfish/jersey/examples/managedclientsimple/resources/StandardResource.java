/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclientsimple.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Standard example resource exposing  GET methods. This resource can be accessed directly by GET and is also
 * accessed from {@link ClientResource} using injected client.
 *
 * @author Miroslav Fuksa
 *
 */
@Path("resource")
public class StandardResource {
    @GET
    @Path("dog")
    public String get() {
        return "Max";
    }

    @GET
    @Path("cat")
    public String cat() {
        return "Lucy";
    }

    @GET
    @Path("elefant")
    public String elefant() {
        return "Bobo";
    }

    /**
     * Returns resource based on the id which is passed as path parameter. For purpose of this sample the method
     * just use the path parameter id to construct a string which is returned.
     *
     * @param id Injected path parameter.
     * @return Resource constructed from given id.
     */
    @GET
    @Path("car/{id}")
    public String car(@PathParam("id") String id) {
        return "CAR with id=" + id;
    }
}
