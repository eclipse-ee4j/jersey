/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.simple;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the parallel execution of multiple requests.
 *
 * @author Stepan Kopriva
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class ParallelTest extends AbstractSimpleServerTester {

    // Server-side dispatcher and selector pool configuration
    private static final int selectorThreads = Runtime.getRuntime().availableProcessors();
    private static final int dispatcherThreads = Math.max(8, selectorThreads * 2);

    private static final int numberOfThreads = 100;

    private static final String PATH = "test";
    private static AtomicInteger receivedCounter = new AtomicInteger(0);
    private static AtomicInteger resourceCounter = new AtomicInteger(0);
    private static CountDownLatch latch = new CountDownLatch(numberOfThreads);

    @Path(PATH)
    public static class MyResource {

        @GET
        public String get() {
            this.sleep();
            resourceCounter.addAndGet(1);
            return "GET";
        }

        private void sleep() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ParallelTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class ResourceThread extends Thread {

        private WebTarget target;
        private String path;

        public ResourceThread(WebTarget target, String path) {
            this.target = target;
            this.path = path;
        }

        @Override
        public void run() {
            assertEquals("GET", target.path(path).request().get(String.class));
            receivedCounter.addAndGet(1);
            latch.countDown();
        }
    }

    @Test
    public void testParallel() {
        ResourceConfig config = new ResourceConfig(MyResource.class);
        startServer(config, dispatcherThreads, selectorThreads);
        WebTarget target = ClientBuilder.newClient().target(getUri().path("/").build());

        for (int i = 1; i <= numberOfThreads; i++) {
            ResourceThread rt = new ResourceThread(target, PATH);
            rt.start();
        }

        try {
            latch.await(8000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(ParallelTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        int result = receivedCounter.get();
        assertEquals(numberOfThreads, result);
    }
}
