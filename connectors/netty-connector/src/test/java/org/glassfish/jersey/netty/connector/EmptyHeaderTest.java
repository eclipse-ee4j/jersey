/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class EmptyHeaderTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(EmptyHeaderTest.class.getName());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new EmptyHeaderTest().testEmptyHeaders();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig().register(new EmptyHeaderTestResource());
    }

    @Test
    public void testEmptyHeaders() throws ExecutionException, InterruptedException {
        MultivaluedMap<String, Object> jersey2Headers = new MultivaluedHashMap();
        jersey2Headers.put("", Collections.singletonList("sss"));

        Entity mData = Entity.entity("{\"dd\":\"ddd\"}", MediaType.APPLICATION_JSON_TYPE);

        ClientConfig config = new ClientConfig();
        config.connectorProvider(new NettyConnectorProvider());
        try {
            Response r = ClientBuilder.newBuilder()
                    .withConfig(config)
                    .build()
                    .target(target().getUri())
                    .request()
                    .headers(jersey2Headers)
                    .post(mData);
            Assertions.fail("Processing Exception not thrown for empty header name");
        } catch (ProcessingException processingException) {
            System.out.println(processingException.getMessage());
        }
    }

    @Path("")
    private static class EmptyHeaderTestResource {
        @GET
        public Response ok() {
            return Response.ok().build();
        }
    }
}
