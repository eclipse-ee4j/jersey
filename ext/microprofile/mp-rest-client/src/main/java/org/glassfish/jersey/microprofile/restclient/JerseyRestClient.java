/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates.
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
package org.glassfish.jersey.microprofile.restclient;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.microprofile.restclient.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;

public class JerseyRestClient extends JerseyClient {

    /**
     * Create a new Jersey client instance.
     *
     * @param config jersey client configuration.
     * @param sslContext jersey client SSL context.
     * @param verifier jersey client host name verifier.
     */
    protected JerseyRestClient(final Configuration config,
            final SSLContext sslContext,
            final HostnameVerifier verifier) {

        super(config, sslContext, verifier, null);
    }

    /**
     * Create a new Jersey client instance.
     *
     * @param config jersey client configuration.
     * @param sslContextProvider jersey client SSL context provider.
     * @param verifier jersey client host name verifier.
     */
    protected JerseyRestClient(final Configuration config,
            final UnsafeValue<SSLContext, IllegalStateException> sslContextProvider,
            final HostnameVerifier verifier) {
        super(config, sslContextProvider, verifier, null);
    }

    @Override
    public JerseyWebTarget target(final String uri) {
        checkNotClosed();
        checkNotNull(uri, LocalizationMessages.CLIENT_URI_TEMPLATE_NULL());
        return new RestClientWebTarget(uri, this);
    }

    @Override
    public JerseyWebTarget target(final URI uri) {
        checkNotClosed();
        checkNotNull(uri, LocalizationMessages.CLIENT_URI_NULL());
        return new RestClientWebTarget(uri, this);
    }

    @Override
    public JerseyWebTarget target(final UriBuilder uriBuilder) {
        checkNotClosed();
        checkNotNull(uriBuilder, LocalizationMessages.CLIENT_URI_BUILDER_NULL());
        return new RestClientWebTarget(uriBuilder, this);
    }

    @Override
    public JerseyWebTarget target(final Link link) {
        checkNotClosed();
        checkNotNull(link, LocalizationMessages.CLIENT_TARGET_LINK_NULL());
        return new RestClientWebTarget(link, this);
    }

    @Override
    public JerseyInvocation.Builder invocation(final Link link) {
        checkNotClosed();
        checkNotNull(link, LocalizationMessages.CLIENT_INVOCATION_LINK_NULL());
        final RestClientWebTarget t = new RestClientWebTarget(link, this);
        final String acceptType = link.getType();
        return (acceptType != null) ? t.request(acceptType) : t.request();
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

}
