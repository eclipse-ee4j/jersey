/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.spring;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Jersey Spring integration example.
 * Demonstrate how to inject a Spring bean into a Jersey managed JAX-RS resource class.
 *
 * @author Marko Asplund (marko.asplund at gmail.com)
 */
@Path("jersey-hello")
public class JerseyResource {
    private static final Logger LOGGER = Logger.getLogger(JerseyResource.class.getName());

    @Autowired
    private GreetingService greetingService;

    @Inject
    private DateTimeService timeService;

    public JerseyResource() {
        LOGGER.fine("HelloWorldResource()");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHello() {
        return String.format("%s: %s", timeService.getDateTime(), greetingService.greet("world"));
    }

}
