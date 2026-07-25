/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.netty.httpserver.Helper.TestEntityWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test to make sure huge entity gets chunk-encoded.
 *
 * @author Jakub Podlesak
 */
public class HugeEntityTest extends JerseyTest {

    public HugeEntityTest() {
      super(new NettyTestContainerFactory());
    }
    /**
     * JERSEY-2337 reproducer. The resource is used to check the right amount of data
     * is being received from the client and also gives us ability to check we receive
     * correct data.
     */
    @Path("/")
    public static class ConsumerResource {

        /**
         * Return back the count of bytes received.
         * This way, we should be able to consume a huge amount of data.
         */
        @POST
        @Path("size")
        public String post(InputStream in) throws IOException {
            return String.valueOf(Helper.drainAndCountInputStream(in));
        }

        @POST
        @Path("echo")
        public String echo(String s) {
            return s;
        }

        @POST
        @Path("buffer_overflow_test")
        @Produces(MediaType.TEXT_PLAIN)
        public Response bufferOverflowTest(InputStream in) throws IOException {
            assertEquals(Helper.ONE_MB_IN_BYTES, Helper.drainAndCountInputStream(in));
            return Response.ok(new ByteArrayInputStream(new byte[Helper.ONE_MB_IN_BYTES]), MediaType.TEXT_PLAIN).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ConsumerResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(TestEntityWriter.class);
        config.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);
        config.connectorProvider(new NettyConnectorProvider());
    }

    public static class TestEntity {

        final long size;

        public TestEntity(long size) {
            this.size = size;
        }
    }
    /**
     * JERSEY-2337 reproducer. We are going to send huge amount of data over the wire.
     * Should not the data have been chunk-encoded, we would easily run out of memory.
     *
     * @throws Exception in case of a test error.
     */
    @Test
    public void testPost() throws Exception {
        Response response = target("/size").request()
                                           .post(Entity.entity(new TestEntity(Helper.TWENTY_GB_IN_BYTES),
                                                               MediaType.APPLICATION_OCTET_STREAM_TYPE));
        String content = response.readEntity(String.class);
        assertThat(Long.parseLong(content), equalTo(Helper.TWENTY_GB_IN_BYTES));

        // just to check the right data have been transfered.
        response = target("/echo").request().post(Entity.text("Hey Sync!"));
        assertThat(response.readEntity(String.class), equalTo("Hey Sync!"));
    }

    /**
     * Tests buffer overflow - https://github.com/eclipse-ee4j/jersey/issues/3500
     * @throws IOException
     *
     * @throws Exception
     */
    @Test
    public void testPostResponse() throws IOException {
        Response response = target("/buffer_overflow_test").request().post(Entity
            .entity(new TestEntity(Helper.ONE_MB_IN_BYTES), MediaType.APPLICATION_OCTET_STREAM_TYPE));
        try {
          InputStream is = response.readEntity(InputStream.class);
          assertEquals(Helper.ONE_MB_IN_BYTES, Helper.drainAndCountInputStream(is));
        } catch (ProcessingException pe) {
          Assert.fail();
        }

        // just to check the right data have been transfered.
        response = target("/echo").request().post(Entity.text("Hey Sync!"));
        assertThat(response.readEntity(String.class), equalTo("Hey Sync!"));
    }

    /**
     * JERSEY-2337 reproducer. We are going to send huge amount of data over the wire. This time in an async fashion.
     * Should not the data have been chunk-encoded, we would easily run out of memory.
     *
     * @throws Exception in case of a test error.
     */
    @Test
    public void testAsyncPost() throws Exception {
        Future<Response> response = target("/size").request().async()
                                                   .post(Entity.entity(new TestEntity(Helper.TWENTY_GB_IN_BYTES),
                                                                       MediaType.APPLICATION_OCTET_STREAM_TYPE));
        final String content = response.get().readEntity(String.class);
        assertThat(Long.parseLong(content), equalTo(Helper.TWENTY_GB_IN_BYTES));

        // just to check the right data have been transfered.
        response = target("/echo").request().async().post(Entity.text("Hey Async!"));
        assertThat(response.get().readEntity(String.class), equalTo("Hey Async!"));
    }
}
