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

package org.glassfish.jersey.tests.e2e.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import javax.inject.Singleton;

import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Marc Hadley
 */
public class CloseableTest extends JerseyTest {
    private static CountDownLatch perRequestCdl = new CountDownLatch(1);
    private static CountDownLatch singletonCdl = new CountDownLatch(1);

    @Path("per-request")
    public static class PerRequestResource implements Closeable {
        static boolean isClosed;

        @Context
        CloseableService cs;

        @GET
        public String doGet() {
            isClosed = false;
            cs.add(this);
            return "ok";
        }

        public void close() throws IOException {
            isClosed = true;
            perRequestCdl.countDown();
        }
    }

    @Path("singleton")
    @Singleton
    public static class SingletonResource extends PerRequestResource {
        @Override
        public void close() {
            isClosed = true;
            singletonCdl.countDown();
        }
    }

    @Override
    public ResourceConfig configure() {
        return new ResourceConfig(PerRequestResource.class, SingletonResource.class);
    }

    @Test
    public void testPerRequest() throws InterruptedException {
        target().path("per-request").request().get(String.class);
        perRequestCdl.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(PerRequestResource.isClosed);
    }

    @Test
    public void testSingleton() throws InterruptedException {
        target().path("singleton").request().get(String.class);
        perRequestCdl.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(SingletonResource.isClosed);
    }

}
