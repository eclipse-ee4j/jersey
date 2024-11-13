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

package org.glassfish.jersey.tests.e2e.tls.connector;

import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Jersey client seems to be not thread-safe:
 * When the first GET request is in progress,
 * all parallel requests from other Jersey client instances fail
 * with SSLHandshakeException: PKIX path building failed.
 * <p>
 * Once the first GET request is completed,
 * all subsequent requests work without error.
 * <p>
 * BUG 5749
 */
public class ConcurrentHttpsUrlConnectionTest {
    private static int THREAD_NUMBER = 5;

    private static volatile int responseCounter = 0;

    private static SSLContext createContext() throws Exception {
        URL url = ConcurrentHttpsUrlConnectionTest.class.getResource("keystore.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream is = url.openStream()) {
            keyStore.load(is, "password".toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, "password".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(keyStore);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return context;
    }

    @Test
    public void testSSLConnections() throws Exception {
        if (THREAD_NUMBER == 1) {
            System.out.println("\nThis is the working case (THREAD_NUMBER==1). Set THREAD_NUMBER > 1 to reproduce the error! \n");
        }

        final HttpsServer server = new HttpsServer(createContext());
        Executors.newFixedThreadPool(1).submit(server);

        // set THREAD_NUMBER > 1 to reproduce an issue
        ExecutorService executorService2clients = Executors.newFixedThreadPool(THREAD_NUMBER);

        final ClientBuilder builder = ClientBuilder.newBuilder().sslContext(createContext())
                .hostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });

        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < THREAD_NUMBER; i++) {
            executorService2clients.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Client client = builder.build();
                        String ret = client.target("https://127.0.0.1:" + server.getPort() + "/" + new Random().nextInt())
                                .request(MediaType.TEXT_HTML)
                                .get(new GenericType<String>() {
                                });
                        System.out.print(++responseCounter + ". Server returned: " + ret);
                    } catch (Exception e) {
                        //get an exception here, if jersey lib is buggy and THREAD_NUMBER > 1:
                        //jakarta.ws.rs.ProcessingException: javax.net.ssl.SSLHandshakeException: PKIX path building failed:
                        e.printStackTrace();
                    } finally {
                        System.out.println(counter.incrementAndGet());
                    }
                }
            });
        }

        while (counter.get() != THREAD_NUMBER) {
            Thread.sleep(100L);
        }
        server.stop();
    }
}
