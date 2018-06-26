/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.container;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * This is really weird approach and test.
 *
 * @author Michal Gajdos
 */
public class ResponseWriterOutputStreamTest extends JerseyContainerTest {

    private static final String CHECK_STRING = "RESOURCE";
    private static final CountDownLatch latch = new CountDownLatch(1);

    @Path("/")
    public static class Resource {

        @GET
        @Produces("text/plain")
        public void get(final ContainerRequest context) throws IOException {
            assertThat(context.getMethod(), is("GET"));

            final ContainerResponse response = new ContainerResponse(context, Response.ok().build());
            final OutputStream os = context.getResponseWriter()
                    .writeResponseStatusAndHeaders(CHECK_STRING.getBytes().length, response);
            os.write(CHECK_STRING.getBytes());
            os.close();
        }

        @POST
        @Produces("text/plain")
        public void post(final ContainerRequest context) throws IOException {
            assertThat(context.getMethod(), is("POST"));

            final String s = context.readEntity(String.class);
            assertEquals(CHECK_STRING, s);

            final ContainerResponse response = new ContainerResponse(context, Response.ok().build());
            try {
                final OutputStream os = context.getResponseWriter()
                        .writeResponseStatusAndHeaders(s.getBytes().length, response);
                os.write(s.getBytes());
                os.close();
            } finally {
                latch.countDown();
            }

        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testGet() {
        assertThat(target().request().get(String.class), is(CHECK_STRING));
    }

    @Test
    public void testPost() throws InterruptedException, ExecutionException {
        final Invocation invocation = target().request().buildPost(Entity.text(CHECK_STRING));
        final Future<Response> resp = invocation.submit();
        latch.await();
        final String response = resp.get().readEntity(String.class);
        assertThat(response, is(CHECK_STRING));
    }

    @Test
    public void testAll() throws InterruptedException, ExecutionException {
        testGet();
        testPost();
    }
}
