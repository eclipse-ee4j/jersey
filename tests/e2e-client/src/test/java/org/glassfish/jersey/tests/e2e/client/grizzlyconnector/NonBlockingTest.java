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

package org.glassfish.jersey.tests.e2e.client.grizzlyconnector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Grizzly connector non blocking test.
 */
public class NonBlockingTest extends JerseyTest {

    @Path("/test")
    public static class Resource {

        @GET
        public String get() {
            return "GET";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new GrizzlyConnectorProvider());
    }

    private volatile String invocationCallbackThreadName;

    @Test
    public void testNonBlockingConnector() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Future<String> future = target("test")
                .request()
                .async()
                .get(new InvocationCallback<String>() {
                    @Override
                    public void completed(String response) {
                        invocationCallbackThreadName = Thread.currentThread().getName();
                        countDownLatch.countDown();
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        invocationCallbackThreadName = Thread.currentThread().getName();
                        countDownLatch.countDown();
                    }
                });

        String response = future.get();
        assertNotNull(response);
        assertTrue("Invocation callback was not invoked",
                countDownLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Invocation callback is not executed on the NIO pool thread.",
                   !invocationCallbackThreadName.contains("jersey-client-async-executor"));
    }
}
