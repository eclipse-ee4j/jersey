/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.rx.rxjava;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import rx.Subscriber;

/**
 * @author Michal Gajdos
 * @author Pavel Bucek
 */
public class RxObservableTest {

    private Client client;
    private Client clientWithExecutor;
    private ExecutorService executor;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient().register(TerminalClientRequestFilter.class);
        client.register(RxObservableInvokerProvider.class);
        executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder()
                .setNameFormat("jersey-rx-client-test-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build());

        clientWithExecutor = ClientBuilder.newBuilder().executorService(executor).build();
        clientWithExecutor.register(TerminalClientRequestFilter.class);
        clientWithExecutor.register(RxObservableInvokerProvider.class);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdown();

        client.close();
        client = null;
    }

    @Test
    public void testNotFoundResponse() throws Exception {
        final RxObservableInvoker invoker = client.target("http://jersey.java.net")
                                                  .request()
                                                  .header("Response-Status", 404)
                                                  .rx(RxObservableInvoker.class);

        testInvoker(invoker, 404, false);
    }

    @Test
    public void testNotFoundWithCustomExecutor() throws Exception {
        final RxObservableInvoker invoker = clientWithExecutor.target("http://jersey.java.net")
                                                              .request()
                                                              .header("Response-Status", 404)
                                                              .rx(RxObservableInvoker.class);

        testInvoker(invoker, 404, true);
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundReadEntityViaClass() throws Throwable {
        try {
            client.target("http://jersey.java.net")
                  .request()
                  .header("Response-Status", 404)
                  .rx(RxObservableInvoker.class)
                  .get(String.class)
                  .toBlocking()
                  .toFuture()
                  .get();
        } catch (final Exception expected) {
            // java.util.concurrent.ExecutionException
            throw expected
                    // javax.ws.rs.ProcessingException
                    // .getCause()
                    // javax.ws.rs.NotFoundException
                    .getCause();
        }
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundReadEntityViaGenericType() throws Throwable {
        try {
            client.target("http://jersey.java.net")
                  .request()
                  .header("Response-Status", 404)
                  .rx(RxObservableInvoker.class)
                  .get(new GenericType<String>() { })
                  .toBlocking()
                  .toFuture()
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
        final String response = client.target("http://jersey.java.net")
                                      .request()
                                      .rx(RxObservableInvoker.class)
                                      .get(String.class)
                                      .toBlocking()
                                      .toFuture()
                                      .get();

        assertThat(response, is("NO-ENTITY"));
    }

    @Test
    public void testReadEntityViaGenericType() throws Throwable {
        final String response = client.target("http://jersey.java.net")
                                      .request()
                                      .rx(RxObservableInvoker.class)
                                      .get(new GenericType<String>() { })
                                      .toBlocking()
                                      .toFuture()
                                      .get();

        assertThat(response, is("NO-ENTITY"));
    }

    private void testInvoker(final RxObservableInvoker rx, final int expectedStatus, final boolean testDedicatedThread)
            throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Response> responseRef = new AtomicReference<>();
        final AtomicReference<Throwable> errorRef = new AtomicReference<>();

        rx.get().subscribe(new Subscriber<Response>() {
            @Override
            public void onCompleted() {
                latch.countDown();
            }

            @Override
            public void onError(final Throwable e) {
                errorRef.set(e);
                latch.countDown();
            }

            @Override
            public void onNext(final Response response) {
                responseRef.set(response);
            }
        });

        latch.await();

        if (errorRef.get() == null) {
            testResponse(responseRef.get(), expectedStatus, testDedicatedThread);
        } else {
            throw (Exception) errorRef.get();
        }
    }

    private static void testResponse(final Response response, final int expectedStatus, final boolean testDedicatedThread) {
        assertThat(response.getStatus(), is(expectedStatus));
        assertThat(response.readEntity(String.class), is("NO-ENTITY"));

        // Executor.
        assertThat(response.getHeaderString("Test-Thread"), testDedicatedThread
                ? containsString("jersey-rx-client-test") : containsString("jersey-client-async-executor"));

        // Properties.
        assertThat(response.getHeaderString("Test-Uri"), is("http://jersey.java.net"));
        assertThat(response.getHeaderString("Test-Method"), is("GET"));
    }
}
