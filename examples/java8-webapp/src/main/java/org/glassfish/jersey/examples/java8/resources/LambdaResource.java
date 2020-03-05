/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.java8.resources;

import java.util.Collections;
import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * JAX-RS resource using Java SE 8 lambdas.
 *
 * @author Marek Potociar
 */
@Path("lambdas/{p}")
public class LambdaResource {
    @GET
    public String getLambdaResult(@PathParam("p") String p) {
        return Collections.singleton(p).stream().map(v -> v + "-lambdaized").collect(Collectors.joining());
    }
}
