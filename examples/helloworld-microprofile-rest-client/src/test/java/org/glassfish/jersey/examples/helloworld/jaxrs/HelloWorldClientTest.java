/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.jersey.examples.helloworld.jaxrs;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.sun.net.httpserver.HttpServer;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

/**
 * Simple test to call HelloWorldResource REST API using HelloWorldClient proxy client.
 *
 * @author Gaurav Gupta
 */
public class HelloWorldClientTest {

    @Test
    public void testHelloWorld() throws Exception {
        HttpServer server = App.startServer();

        HelloWorldClient simpleGetApi = RestClientBuilder.newBuilder()
            .baseUri(App.getBaseURI())
            .build(HelloWorldClient.class);

        assertEquals(HelloWorldResource.CLICHED_MESSAGE, simpleGetApi.getHello());

        server.stop(0);
    }
}
