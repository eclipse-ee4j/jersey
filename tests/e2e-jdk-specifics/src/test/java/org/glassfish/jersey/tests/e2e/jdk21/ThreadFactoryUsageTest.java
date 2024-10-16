/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.jdk21;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.innate.VirtualThreadSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadFactoryUsageTest {
    @Test
    public void testThreadFactory() throws ExecutionException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ThreadFactory threadFactory = VirtualThreadSupport.allowVirtual(true).getThreadFactory();
        ThreadFactory countDownThreadFactory = r -> {
            countDownLatch.countDown();
            return threadFactory.newThread(r);
        };

        CompletionStage<Response> r = ClientBuilder.newClient()
                .property(CommonProperties.THREAD_FACTORY, countDownThreadFactory)
                .property(CommonProperties.USE_VIRTUAL_THREADS, true)
                .register((ClientRequestFilter) requestContext -> requestContext.abortWith(Response.ok().build()))
                .target("http://localhost:58080/test").request().rx().get();

        MatcherAssert.assertThat(r.toCompletableFuture().get().getStatus(), Matchers.is(200));
        countDownLatch.await(10, TimeUnit.SECONDS);
        MatcherAssert.assertThat(countDownLatch.getCount(), Matchers.is(0L));
    }
}
