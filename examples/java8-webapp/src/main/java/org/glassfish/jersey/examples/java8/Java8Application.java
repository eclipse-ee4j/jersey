/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.java8;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.examples.java8.resources.DefaultMethodResource;
import org.glassfish.jersey.examples.java8.resources.LambdaResource;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Application for illustrating some of the features of Java 8 in JAX-RS.
 *
 * @author Michal Gajdos
 */
@ApplicationPath("j8")
public class Java8Application extends ResourceConfig {

    public Java8Application() {
        // Resources.
        register(DefaultMethodResource.class);
        register(LambdaResource.class);
    }
}
