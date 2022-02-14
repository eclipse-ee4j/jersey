/*
 * Copyright (c) 2021, 2022 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.grizzly2.httpserver;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.jersey.grizzly2.httpserver.test.tools.ClientThread;
import org.glassfish.jersey.grizzly2.httpserver.test.tools.ClientThread.ClientThreadSettings;
import org.glassfish.jersey.grizzly2.httpserver.test.tools.JdkHttpClientThread;
import org.glassfish.jersey.grizzly2.httpserver.test.tools.ServerManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test verifying stability of the {@link GrizzlyHttpContainer} having to serve many requests
 * and also giving several examples of how to configure HTTP, HTTPS and HTTP/2 clients.
 * <p>
 * Created as an attempt to reproduce Grizzly's issue #2125 (GitHub)
 *
 * @author David Matejcek
 */
public class GrizzlyHttpServerTest {

    private static final String CLIENT_IMPLEMENTATION = System.getProperty("clientImpl",
        JdkHttpClientThread.class.getCanonicalName());
    private static final long TIME_IN_MILLIS = Long.getLong("testTime", 10L) * 1000L;
    private static final int COUNT_OF_CLIENTS = Integer.getInteger("clientCount", 20);

    private final List<ClientThread> clients = new ArrayList<>(COUNT_OF_CLIENTS);
    private AtomicReference<Throwable> error = new AtomicReference<>();
    private AtomicInteger counter;
    private ServerManager server;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void printSettings() {
        System.out.println("Client implementation: " + CLIENT_IMPLEMENTATION);
        System.out.println("Count of clients: " + COUNT_OF_CLIENTS);
        System.out.println("Test duration: " + TIME_IN_MILLIS / 1000 + " s");
    }


    @Before
    public void init() {
        this.counter = new AtomicInteger();
    }


    @After
    public void cleanup() {
        error = null;
        System.out.println(String.format("Server processed %s requests of test %s.", counter, testName.getMethodName()));
        if (server != null) {
            server.close();
        }
    }


    /**
     * Test for unsecured HTTP 1.1 protocol.
     *
     * @throws Throwable
     */
    @Test
    public void http() throws Throwable {
        final boolean secured = false;
        final boolean useHttp2 = false;
        this.server = new ServerManager(secured, useHttp2);
        this.clients.addAll(createClients(secured, useHttp2));
        executeTest();
    }


    /**
     * Test for HTTP 1.1 protocol encrypted by {@value ClientThread#ENCRYPTION_PROTOCOL}.
     *
     * @throws Throwable
     */
    @Test
    public void https() throws Throwable {
        final boolean secured = true;
        final boolean useHttp2 = false;
        this.server = new ServerManager(secured, useHttp2);
        this.clients.addAll(createClients(secured, useHttp2));
        executeTest();
    }


    /**
     * This test is rather for documentaion purpose, because HTTP/2 is usually not allowed to be
     * used without encryption. Remember that.
     *
     * @throws Throwable
     */
    @Test(expected = IllegalArgumentException.class)
    public void http2() throws Throwable {
        this.server = new ServerManager(false, true);
    }


    /**
     * Test for HTTP/2 protocol encrypted by {@value ClientThread#ENCRYPTION_PROTOCOL}.
     *
     * @throws Throwable
     */
    @Test
    public void https2() throws Throwable {
        final boolean secured = true;
        final boolean useHttp2 = true;
        this.server = new ServerManager(secured, useHttp2);
        this.clients.addAll(createClients(secured, useHttp2));
        executeTest();
    }


    private void executeTest() throws Throwable {
        for (final ClientThread clientThread : clients) {
            clientThread.start();
        }
        final long start = System.currentTimeMillis();
        while (error.get() == null && System.currentTimeMillis() < start + TIME_IN_MILLIS) {
            Thread.yield();
        }
        for (final ClientThread clientThread : clients) {
            clientThread.stopClient();
        }
        for (final ClientThread clientThread : clients) {
            // cycles are fast, so we can afford this.
            clientThread.join(100L);
        }
        if (error.get() != null) {
            throw error.get();
        }
        assertTrue("No requests processed.", counter.get() > 0);
    }


    private Collection<ClientThread> createClients(final boolean secured, final boolean useHttp2) throws Exception {
        final List<ClientThread> list = new ArrayList<>(COUNT_OF_CLIENTS);
        for (int i = 0; i < COUNT_OF_CLIENTS; i++) {
            list.add(createClient(secured, useHttp2, i + 1));
        }
        return list;
    }


    private ClientThread createClient(final boolean secured, final boolean useHttp2, final int id) throws Exception {
        @SuppressWarnings("unchecked")
        final Class<ClientThread> clazz = (Class<ClientThread>) Class.forName(CLIENT_IMPLEMENTATION);
        final Constructor<ClientThread> constructor = clazz.getConstructor(ClientThreadSettings.class,
            AtomicInteger.class, AtomicReference.class);
        assertNotNull("constructor for " + CLIENT_IMPLEMENTATION, constructor);
        final ClientThreadSettings settings = new ClientThreadSettings(id, secured, useHttp2,
            server.getApplicationServiceEndpoint());
        return constructor.newInstance(settings, counter, error);
    }
}
