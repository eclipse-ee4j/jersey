/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reproduces <a href="https://github.com/eclipse-ee4j/jersey/issues/6125">issue #6125</a>:
 * {@code NettyConnector} creates a new {@code JerseyExpectContinueHandler} instance for every request.
 * If the request carries an {@code Expect: 100-continue} header, the connector gives a latch to
 * this instance and waits on that latch. The latch can be released by the handler that
 * receives the {@code 100 Continue} response. To receive the {@code 100 Continue} response, the handler
 * must be in the pipeline. But the handler is only added to the pipeline when a <b>new</b> channel
 * is created. On a channel taken from the pool, the pipeline holds a different handler:
 * the one that was created for the first request on that channel.
 * That handler has no latch, so it releases nothing.
 * <p>
 * As a result, every {@code Expect: 100-continue} request sent on a reused channel waits until
 * the timeout expires, even if the {@code 100 Continue} response arrives immediately.
 * The connector handles the timeout by writing an empty last chunk,
 * so the server sees an empty finished request. After writing the last chunk,
 * the connector sends the request again without the {@code Expect: 100-continue} header, as a fallback.
 * This is how one JAX-RS call becomes two requests.
 */
public class Expect100ContinueOnPooledChannelTest extends JerseyTest {

    private static final int REQUEST_COUNT = 4;
    private static final byte[] ENTITY = new byte[256 * 1024];

    private static final List<Integer> RECEIVED = Collections.synchronizedList(new ArrayList<>());

    private static volatile CountDownLatch received = new CountDownLatch(REQUEST_COUNT);

    @Path("/upload")
    public static class UploadResource {
        @PUT
        @Path("{name}")
        public Response put(@PathParam("name") String name, byte[] entity) {
            RECEIVED.add(entity == null ? 0 : entity.length);
            received.countDown();
            return Response.ok().build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(UploadResource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new NettyConnectorProvider())
                .property(ClientProperties.EXPECT_100_CONTINUE, true)
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);
    }

    @BeforeEach
    public void clearReceived() {
        RECEIVED.clear();
        received = new CountDownLatch(REQUEST_COUNT);
    }

    @Test
    public void testOneRequestPerCallOnPooledChannel() throws InterruptedException {
        for (int i = 1; i <= REQUEST_COUNT; i++) {
            try (Response response = target("upload").path("entity-" + i).request()
                    .put(Entity.entity(ENTITY, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {
                Assertions.assertEquals(200, response.getStatus(), "request " + i + " failed");
            }
        }
        Assertions.assertTrue(received.await(10 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS),
                "the server did not receive all the requests in time: " + RECEIVED.size()
                        + " of " + REQUEST_COUNT);
        Assertions.assertFalse(RECEIVED.contains(0),
                "the resource was invoked with an empty body, the requests it received: " + RECEIVED);
        Assertions.assertEquals(REQUEST_COUNT, RECEIVED.size(),
                "the resource was invoked " + RECEIVED.size() + " times for " + REQUEST_COUNT + " calls: " + RECEIVED);
    }
}
