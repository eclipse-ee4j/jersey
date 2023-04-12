/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

public class BufferedTest extends JerseyTest {

    private static String HEADER_1 = "First";
    private static String HEADER_2 = "Second";
    private static String HEADER_3 = "Third";
    private static String ENTITY = "entity";


    @Path("/buffered")
    public static class BufferedTestResource {
        @POST
        public String post(@Context HttpHeaders headers, String entity) {
            System.out.println("Remote");
            String ret = headers.getHeaderString(HEADER_1)
                    + headers.getHeaderString(HEADER_2)
                    + headers.getHeaderString(HEADER_3)
                    + entity;
            System.out.println(ret);
            return ret;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(BufferedTestResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider())
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
    }

    @Test
    public void test() {
        try (Response r = target("buffered")
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.setEntity(ENTITY);
                        requestContext.getHeaders().add(HEADER_2, HEADER_2);
                    }
                })
                .register(new WriterInterceptor() {
                    @Override
                    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
                        context.getHeaders().add(HEADER_3, HEADER_3);
                        context.proceed();
                    }
                })
                .request()
                .header(HEADER_1, HEADER_1)
                .post(Entity.entity("ENTITY", MediaType.TEXT_PLAIN_TYPE))) {
            String response = r.readEntity(String.class);
            Assertions.assertEquals(HEADER_1 + HEADER_2 + HEADER_3 + ENTITY, response);
        }
    }
}
