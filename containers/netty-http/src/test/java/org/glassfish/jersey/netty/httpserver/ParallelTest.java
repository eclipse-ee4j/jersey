/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.netty.httpserver.Helper.TestEntityWriter;
import org.glassfish.jersey.netty.httpserver.HugeEntityTest.TestEntity;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the parallel execution of multiple requests.
 *
 * @author Stepan Kopriva
 */
public class ParallelTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(ParallelTest.class.getName());

    private static final int PARALLEL_CLIENTS = 10;
    private static final String PATH = "test";
    private static final AtomicInteger receivedCounter = new AtomicInteger(0);
    private static final AtomicInteger resourceCounter = new AtomicInteger(0);
    private static final CyclicBarrier startBarrier = new CyclicBarrier(PARALLEL_CLIENTS + 1);
    private static final CountDownLatch doneLatch = new CountDownLatch(PARALLEL_CLIENTS);

    public ParallelTest() {
      super(new NettyTestContainerFactory());
    }

    @Path(PATH)
    public static class MyResource {

        @GET
        public String get() {
            sleep();
            resourceCounter.addAndGet(1);
            return "GET";
        }

        /**
         * If connections are leaked, this test will fail.
         * Validates https://github.com/eclipse-ee4j/jersey/issues/3568
         *
         * @return
         */
        @POST
        @Produces(MediaType.TEXT_PLAIN)
        public Response post(InputStream in) throws IOException {
            sleep();
            resourceCounter.addAndGet(1);
            return Response.ok(new ByteArrayInputStream(new byte[Helper.ONE_MB_IN_BYTES]), MediaType.TEXT_PLAIN).build();
        }

        private void sleep() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ParallelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
      config.register(TestEntityWriter.class);
      config.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);
      config.connectorProvider(new NettyConnectorProvider());
    }

    @Test
    public void testParallel() throws BrokenBarrierException, InterruptedException, TimeoutException, IOException {
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(PARALLEL_CLIENTS);
        try {
            final WebTarget target = target();
            for (int i = 1; i <= PARALLEL_CLIENTS; i++) {
                final int id = i;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startBarrier.await();
                            Response response;
                            response = target.path(PATH).request().post(Entity.entity(
                                new TestEntity(Helper.ONE_MB_IN_BYTES), MediaType.APPLICATION_OCTET_STREAM_TYPE));
                            assertEquals(Helper.ONE_MB_IN_BYTES,
                                Helper.drainAndCountInputStream(response.readEntity(InputStream.class)));
                            receivedCounter.incrementAndGet();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            LOGGER.log(Level.WARNING, "Client thread " + id + " interrupted.", ex);
                        } catch (BrokenBarrierException ex) {
                            LOGGER.log(Level.INFO, "Client thread " + id + " failed on broken barrier.", ex);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            LOGGER.log(Level.WARNING, "Client thread " + id + " failed on unexpected exception.", t);
                        } finally {
                            doneLatch.countDown();
                        }
                    }
                });
            }

            startBarrier.await(1, TimeUnit.SECONDS);

            assertTrue("Waiting for clients to finish has timed out.", doneLatch.await(5 * getAsyncTimeoutMultiplier(),
                                                                                       TimeUnit.SECONDS));

            assertEquals("Resource counter", PARALLEL_CLIENTS, resourceCounter.get());

            assertEquals("Received counter", PARALLEL_CLIENTS, receivedCounter.get());
        } finally {
            executor.shutdownNow();
            Assert.assertTrue("Executor termination", executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
