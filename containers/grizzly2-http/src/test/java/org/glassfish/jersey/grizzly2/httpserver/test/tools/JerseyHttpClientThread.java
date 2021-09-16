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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Jersey doesn't support HTTP/2 at this moment, but this class may be extended later.
 * Even this way it passes all tests, because server is still able to use HTTP 1.1 despite we
 * configured it to use HTTP/2
 *
 * @author David Matejcek
 */
public class JerseyHttpClientThread extends ClientThread {

    private final Client client;

    public JerseyHttpClientThread(final ClientThreadSettings settings,
        final AtomicInteger counter, final AtomicReference<Throwable> error) throws Exception {
        super(settings, counter, error);
        this.client = createClient(settings.secured, settings.useHttp2);
    }


    @Override
    public void doGetAndCheckResponse() throws Throwable {
        final WebTarget path = client.target(getSettings().targetUri.toString());
        final Builder builder = path.request();
        final Response response = builder.get();
        final String responseMsg = response.readEntity(String.class);
        assertEquals(200, response.getStatus());
        assertEquals("Got it!", responseMsg);
    }


    private static Client createClient(final boolean secured, final boolean useHttp2) throws Exception {
        final ClientBuilder builder = ClientBuilder.newBuilder().withConfig(new ClientConfig());
        if (secured) {
            builder.sslContext(createSslContext());
        }
        return builder.build();
    }
}
