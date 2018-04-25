/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * JAX-RS resource using Java SE 8 lambdas.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("lambdas/{p}")
public class LambdaResource {
    @GET
    public String getLambdaResult(@PathParam("p") String p) {
        return Collections.singleton(p).stream().map(v -> v + "-lambdaized").collect(Collectors.joining());
    }
}
