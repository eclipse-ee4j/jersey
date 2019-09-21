/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.groovy

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig

import javax.ws.rs.core.UriBuilder

/*
 * Groovy script to start the example app
 */
baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build()
server = GrizzlyHttpServerFactory.createHttpServer(baseUri, new ResourceConfig(GroovyResource.class))

System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n"
        + "Try out %sgroovy\nHit  enter to stop it...", baseUri, baseUri));

System.in.read()
server.shutdown()
