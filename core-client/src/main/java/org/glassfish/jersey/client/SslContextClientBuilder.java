/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.client.spi.DefaultSslContextProvider;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * <p>The class that builds {@link SSLContext} for the client from keystore, truststore. Provides a cached
 * {@link Supplier} from the built or user provided {@link SSLContext}.</p>
 *
 * <p>The class is used internally by {@link JerseyClientBuilder}, or it can be used by connectors supporting setting
 * the {@link SSLContext} per request.</p>
 *
 * @see javax.ws.rs.client.ClientBuilder#keyStore(KeyStore, char[])
 * @see javax.ws.rs.client.ClientBuilder#keyStore(KeyStore, String)
 * @see javax.ws.rs.client.ClientBuilder#sslContext(SSLContext)
 */
public final class SslContextClientBuilder implements Supplier<SSLContext> {
    private SslConfigurator sslConfigurator = null;
    private SSLContext sslContext = null;
    private DefaultSslContextProvider defaultSslContextProvider = null;
    private final Supplier<SSLContext> suppliedValue = Values.lazy((Value<SSLContext>) () -> supply());

    private static final DefaultSslContextProvider DEFAULT_SSL_CONTEXT_PROVIDER = new DefaultSslContextProvider() {
        @Override
        public SSLContext getDefaultSslContext() {
            return SslConfigurator.getDefaultContext();
        }
    };

    /**
     * Set the SSL context that will be used when creating secured transport connections
     * to server endpoints from {@link WebTarget web targets} created by the client
     * instance that is using this SSL context. The SSL context is expected to have all the
     * security infrastructure initialized, including the key and trust managers.
     * <p>
     * Setting a SSL context instance resets any {@link #keyStore(java.security.KeyStore, char[])
     * key store} or {@link #trustStore(java.security.KeyStore) trust store} values previously
     * specified.
     * </p>
     *
     * @param sslContext secure socket protocol implementation which acts as a factory
     *                   for secure socket factories or {@link javax.net.ssl.SSLEngine
     *                   SSL engines}. Must not be {@code null}.
     * @return an updated ssl client context builder instance.
     * @throws NullPointerException in case the {@code sslContext} parameter is {@code null}.
     * @see #keyStore(java.security.KeyStore, char[])
     * @see #keyStore(java.security.KeyStore, String)
     * @see #trustStore
     */
    public SslContextClientBuilder sslContext(SSLContext sslContext) {
        if (sslContext == null) {
            throw new NullPointerException(LocalizationMessages.NULL_SSL_CONTEXT());
        }
        this.sslContext = sslContext;
        sslConfigurator = null;
        return this;
    }

    /**
     * Set the client-side key store. Key store contains client's private keys, and the certificates with their
     * corresponding public keys.
     * <p>
     * Setting a key store instance resets any {@link #sslContext(javax.net.ssl.SSLContext) SSL context instance}
     * value previously specified.
     * </p>
     * <p>
     * Note that for improved security of working with password data and avoid storing passwords in Java string
     * objects, the {@link #keyStore(java.security.KeyStore, char[])} version of the method can be utilized.
     * Also note that a custom key store is only required if you want to enable a custom setup of a 2-way SSL
     * connections (client certificate authentication).
     * </p>
     *
     * @param keyStore client-side key store. Must not be {@code null}.
     * @param password client key password. Must not be {@code null}.
     * @return an updated ssl client context builder instance.
     * @throws NullPointerException in case any of the supplied parameters is {@code null}.
     * @see #sslContext
     * @see #keyStore(java.security.KeyStore, char[])
     * @see #trustStore
     */
    public SslContextClientBuilder keyStore(KeyStore keyStore, char[] password) {
        if (keyStore == null) {
            throw new NullPointerException(LocalizationMessages.NULL_KEYSTORE());
        }
        if (password == null) {
            throw new NullPointerException(LocalizationMessages.NULL_KEYSTORE_PASWORD());
        }
        if (sslConfigurator == null) {
            sslConfigurator = SslConfigurator.newInstance();
        }
        sslConfigurator.keyStore(keyStore);
        sslConfigurator.keyPassword(password);
        sslContext = null;
        return this;
    }

    /**
     * Set the client-side trust store. Trust store is expected to contain certificates from other parties
     * the client is you expect to communicate with, or from Certificate Authorities that are trusted to
     * identify other parties.
     * <p>
     * Setting a trust store instance resets any {@link #sslContext(javax.net.ssl.SSLContext) SSL context instance}
     * value previously specified.
     * </p>
     * <p>
     * In case a custom trust store or custom SSL context is not specified, the trust management will be
     * configured to use the default Java runtime settings.
     * </p>
     *
     * @param trustStore client-side trust store. Must not be {@code null}.
     * @return an updated ssl client context builder instance.
     * @throws NullPointerException in case the supplied trust store parameter is {@code null}.
     * @see #sslContext
     * @see #keyStore(java.security.KeyStore, char[])
     * @see #keyStore(java.security.KeyStore, String)
     */
    public SslContextClientBuilder trustStore(KeyStore trustStore) {
        if (trustStore == null) {
            throw new NullPointerException(LocalizationMessages.NULL_TRUSTSTORE());
        }
        if (sslConfigurator == null) {
            sslConfigurator = SslConfigurator.newInstance();
        }
        sslConfigurator.trustStore(trustStore);
        sslContext = null;
        return this;
    }

    /**
     * Set the client-side key store. Key store contains client's private keys, and the certificates with their
     * corresponding public keys.
     * <p>
     * Setting a key store instance resets any {@link #sslContext(javax.net.ssl.SSLContext) SSL context instance}
     * value previously specified.
     * </p>
     * <p>
     * Note that for improved security of working with password data and avoid storing passwords in Java string
     * objects, the {@link #keyStore(java.security.KeyStore, char[])} version of the method can be utilized.
     * Also note that a custom key store is only required if you want to enable a custom setup of a 2-way SSL
     * connections (client certificate authentication).
     * </p>
     *
     * @param keyStore client-side key store. Must not be {@code null}.
     * @param password client key password. Must not be {@code null}.
     * @return an updated ssl client context builder instance.
     * @throws NullPointerException in case any of the supplied parameters is {@code null}.
     * @see #sslContext
     * @see #keyStore(java.security.KeyStore, char[])
     * @see #trustStore
     */
    public SslContextClientBuilder keyStore(final KeyStore keyStore, final String password) {
        return keyStore(keyStore, password.toCharArray());
    }

    /**
     * Get information about used {@link SSLContext}.
     *
     * @return {@code true} when used {@code SSLContext} is acquired from {@link SslConfigurator#getDefaultContext()},
     * {@code false} otherwise.
     */
    public boolean isDefaultSslContext() {
        return sslContext == null && sslConfigurator == null;
    }

    /**
     * Supply SSLContext from this builder.
     * @return {@link SSLContext}
     */
    @Override
    public SSLContext get() {
        return suppliedValue.get();
    }

    /**
     * Build SSLContext from the Builder.
     * @return {@link SSLContext}
     */
    public SSLContext build() {
        return suppliedValue.get();
    }

    /**
     * Set the default SSL context provider.
     * @param defaultSslContextProvider the default SSL context provider.
     * @return an updated ssl client context builder instance.
     */
    protected SslContextClientBuilder defaultSslContextProvider(DefaultSslContextProvider defaultSslContextProvider) {
        this.defaultSslContextProvider = defaultSslContextProvider;
        return this;
    }

    /**
     * Supply the {@link SSLContext} to the supplier. Can throw illegal state exception when there is a problem with creating or
     * obtaining default SSL context.
     * @return SSLContext
     */
    private SSLContext supply() {
        final SSLContext providedValue;
        if (sslContext != null) {
            providedValue = sslContext;
        } else if (sslConfigurator != null) {
            final SslConfigurator sslConfiguratorCopy = sslConfigurator.copy();
            providedValue = sslConfiguratorCopy.createSSLContext();
        } else {
            providedValue = null;
        }

        final SSLContext returnValue;
        if (providedValue == null) {
            if (defaultSslContextProvider != null) {
                returnValue = defaultSslContextProvider.getDefaultSslContext();
            } else {
                final DefaultSslContextProvider lookedUpSslContextProvider;

                final Iterator<DefaultSslContextProvider> iterator =
                        ServiceFinder.find(DefaultSslContextProvider.class).iterator();

                if (iterator.hasNext()) {
                    lookedUpSslContextProvider = iterator.next();
                } else {
                    lookedUpSslContextProvider = DEFAULT_SSL_CONTEXT_PROVIDER;
                }

                returnValue = lookedUpSslContextProvider.getDefaultSslContext();
            }
        } else {
            returnValue = providedValue;
        }

        return returnValue;
    }
}
