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

package org.glassfish.jersey.client.rx.guava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * @author Michal Gajdos
 */
public class RxListenableFutureTest {

    private Client client;
    private ExecutorService executor;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient().register(TerminalClientRequestFilter.class);
        executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder()
                .setNameFormat("jersey-rx-client-test-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build());
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdown();
        client = null;
    }

    @Test
    public void testNotFoundResponse() throws Exception {
        client.register(RxListenableFutureInvokerProvider.class);

        final RxListenableFutureInvoker invoker = client.target("http://jersey.java.net")
                                                        .request()
                                                        .header("Response-Status", 404)
                                                        .rx(RxListenableFutureInvoker.class);

        testInvoker(invoker, 404, false);
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundReadEntityViaClass() throws Throwable {
        client.register(RxListenableFutureInvokerProvider.class);

        try {
            client.target("http://jersey.java.net")
                  .request()
                  .header("Response-Status", 404)
                  .rx(RxListenableFutureInvoker.class)
                  .get(String.class)
                  .get();
        } catch (final Exception expected) {

            // java.util.concurrent.ExecutionException
            throw expected
                    // javax.ws.rs.NotFoundException
                    .getCause();
        }
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundReadEntityViaGenericType() throws Throwable {
        client.register(RxListenableFutureInvokerProvider.class);

        try {
            client.target("http://jersey.java.net")
                  .request()
                  .header("Response-Status", 404)
                  .rx(RxListenableFutureInvoker.class)
                  .get(new GenericType<String>() {
                  })
                  .get();
        } catch (final Exception expected) {

            expected.printStackTrace();

            // java.util.concurrent.ExecutionException
            throw expected
                    // javax.ws.rs.NotFoundException
                    .getCause();
        }
    }

    @Test
    public void testReadEntityViaClass() throws Throwable {
        client.register(RxListenableFutureInvokerProvider.class);

        final String response = client.target("http://jersey.java.net")
                                      .request()
                                      .rx(RxListenableFutureInvoker.class)
                                      .get(String.class)
                                      .get();

        assertThat(response, is("NO-ENTITY"));
    }

    @Test
    public void testReadEntityViaGenericType() throws Throwable {
        client.register(RxListenableFutureInvokerProvider.class);

        final String response = client.target("http://jersey.java.net")
                                      .request()
                                      .rx(RxListenableFutureInvoker.class)
                                      .get(new GenericType<String>() {
                                      })
                                      .get();

        assertThat(response, is("NO-ENTITY"));
    }

    private void testInvoker(final RxListenableFutureInvoker rx,
                             final int expectedStatus,
                             final boolean testDedicatedThread) throws Exception {
        testResponse(rx.get().get(), expectedStatus, testDedicatedThread);
    }

    private static void testResponse(final Response response, final int expectedStatus, final boolean testDedicatedThread) {
        assertThat(response.getStatus(), is(expectedStatus));
        assertThat(response.readEntity(String.class), is("NO-ENTITY"));

        // Executor.
        final Matcher<String> matcher = containsString("jersey-rx-client-test");
        assertThat(response.getHeaderString("Test-Thread"), testDedicatedThread ? matcher : not(matcher));

        // Properties.
        assertThat(response.getHeaderString("Test-Uri"), is("http://jersey.java.net"));
        assertThat(response.getHeaderString("Test-Method"), is("GET"));
    }
}
