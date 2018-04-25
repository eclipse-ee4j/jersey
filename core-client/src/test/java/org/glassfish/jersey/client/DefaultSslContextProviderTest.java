/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.spi.DefaultSslContextProvider;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Values;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class DefaultSslContextProviderTest {

    @Test
    public void testProvidedDefaultSslContextProvider() {

        final ClientConfig clientConfig = new ClientConfig();

        final AtomicBoolean getDefaultSslContextCalled = new AtomicBoolean(false);


        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, SslConfigurator.getDefaultContext(),
                                 null, new DefaultSslContextProvider() {
                    @Override
                    public SSLContext getDefaultSslContext() {
                        getDefaultSslContextCalled.set(true);

                        return SslConfigurator.getDefaultContext();
                    }
                });

        jerseyClient.getSslContext();

        assertFalse(getDefaultSslContextCalled.get());
        assertFalse(jerseyClient.isDefaultSslContext());
    }

    @Test
    public void testProvidedDefaultSslContextProviderUnsafeVal() {

        final ClientConfig clientConfig = new ClientConfig();

        final AtomicBoolean getDefaultSslContextCalled = new AtomicBoolean(false);


        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, Values.<SSLContext,
                        IllegalStateException>unsafe(SslConfigurator.getDefaultContext()),
                                 null, new DefaultSslContextProvider() {
                    @Override
                    public SSLContext getDefaultSslContext() {
                        getDefaultSslContextCalled.set(true);

                        return SslConfigurator.getDefaultContext();
                    }
                });

        jerseyClient.getSslContext();

        assertFalse(getDefaultSslContextCalled.get());
        assertFalse(jerseyClient.isDefaultSslContext());
    }

    @Test
    public void testCustomDefaultSslContextProvider() {

        final ClientConfig clientConfig = new ClientConfig();

        final AtomicBoolean getDefaultSslContextCalled = new AtomicBoolean(false);
        final AtomicReference<SSLContext> returnedContext = new AtomicReference<SSLContext>(null);

        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, (SSLContext) null,
                                 null, new DefaultSslContextProvider() {
                    @Override
                    public SSLContext getDefaultSslContext() {
                        getDefaultSslContextCalled.set(true);

                        final SSLContext defaultSslContext = SslConfigurator.getDefaultContext();
                        returnedContext.set(defaultSslContext);
                        return defaultSslContext;
                    }
                });

        // make sure context is created
        jerseyClient.getSslContext();

        assertEquals(returnedContext.get(), jerseyClient.getSslContext());
        assertTrue(getDefaultSslContextCalled.get());
        assertTrue(jerseyClient.isDefaultSslContext());
    }

    @Test
    public void testCustomDefaultSslContextProviderUnsafeVal() {

        final ClientConfig clientConfig = new ClientConfig();

        final AtomicBoolean getDefaultSslContextCalled = new AtomicBoolean(false);
        final AtomicReference<SSLContext> returnedContext = new AtomicReference<SSLContext>(null);

        final JerseyClient jerseyClient =
                new JerseyClient(clientConfig, (UnsafeValue<SSLContext, IllegalStateException>) null,
                                 null, new DefaultSslContextProvider() {
                    @Override
                    public SSLContext getDefaultSslContext() {
                        getDefaultSslContextCalled.set(true);

                        final SSLContext defaultSslContext = SslConfigurator.getDefaultContext();
                        returnedContext.set(defaultSslContext);
                        return defaultSslContext;
                    }
                });

        // make sure context is created
        jerseyClient.getSslContext();

        assertEquals(returnedContext.get(), jerseyClient.getSslContext());
        assertTrue(getDefaultSslContextCalled.get());
        assertTrue(jerseyClient.isDefaultSslContext());
    }
}
