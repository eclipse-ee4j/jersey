/*
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.httpserver.test.tools;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Why this? To simulate parallel client access - several clients intensively sending requests
 * and verifying responses.
 *
 * @author David Matejcek
 */
public abstract class ClientThread extends Thread {

    /** Encryption used if you enable secured communication */
    public static final String ENCRYPTION_PROTOCOL = "TLSv1.2";

    private final ClientThreadSettings settings;
    private final AtomicInteger counter;
    private final AtomicReference<Throwable> error;
    private volatile boolean stop;



    public ClientThread(final ClientThreadSettings settings, final AtomicInteger counter,
        final AtomicReference<Throwable> error) throws Exception {
        this.settings = settings;
        this.counter = counter;
        this.error = error;
        setName("client-" + settings.id);
        setUncaughtExceptionHandler((t, e) -> {
            stop = true;
            error.compareAndSet(null, e);
        });
    }


    /**
     * @return {@link ClientThreadSettings}
     */
    protected final ClientThreadSettings getSettings() {
        return this.settings;
    }


    /**
     * Executes the requests and checks the response.
     *
     * @throws Throwable
     */
    protected abstract void doGetAndCheckResponse() throws Throwable;


    /**
     * If the client is stateful, override this method.
     */
    protected void disconnect() {
        // by default nothing to do.
    }


    /**
     * Instructs the client thread to stop when possible.
     */
    public void stopClient() {
        this.stop = true;
    }


    @Override
    public final void run() {
        try {
            // stop when asked to stop or any "brother" thread observed throwable
            while (!stop && error.get() == null) {
                doGetAndCheckResponse();
                counter.incrementAndGet();
            }
        } catch (final Throwable t) {
            throw new IllegalStateException("The client thread failed: " + getName(), t);
        } finally {
            disconnect();
        }
    }


    /**
     * @return Trivial {@link SSLContext}, accepting all certificates and host names
     * @throws GeneralSecurityException
     */
    protected static SSLContext createSslContext() throws GeneralSecurityException {
        final SSLContext ctx = SSLContext.getInstance(ENCRYPTION_PROTOCOL);
        ctx.init(null, new TrustManager[] {new NaiveTrustManager()}, null);
        return ctx;
    }

    /**
     * Simplified configuration of the client thread.
     */
    public static class ClientThreadSettings {

        /** Id of the client thread */
        public final int id;
        /** True if the connection will be encrypted */
        public final boolean secured;
        /** True if the protocol should be HTTP/2, false for HTTP 1.1 */
        public final boolean useHttp2;
        /** The endpoint {@link URI} of the servlet */
        public final URI targetUri;

        /**
         * Creates simplified configuration of the client thread.
         *
         * @param id id of the client thread
         * @param secured true if the connection will be encrypted
         * @param useHttp2 true if the protocol should be HTTP/2, false for HTTP 1.1
         * @param targetUri the endpoint {@link URI} of the servlet
         */
        public ClientThreadSettings(final int id, final boolean secured, final boolean useHttp2, final URI targetUri) {
            this.id = id;
            this.secured = secured;
            this.useHttp2 = useHttp2;
            this.targetUri = targetUri;
        }
    }
}
