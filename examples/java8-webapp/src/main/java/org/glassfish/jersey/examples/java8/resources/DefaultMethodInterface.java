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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Example interface containing resource methods in form of Java8's default methods.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public interface DefaultMethodInterface {

    @GET
    default String root() {
        return "interface-root";
    }

    @GET
    @Path("path")
    default String path() {
        return "interface-path";
    }
}

