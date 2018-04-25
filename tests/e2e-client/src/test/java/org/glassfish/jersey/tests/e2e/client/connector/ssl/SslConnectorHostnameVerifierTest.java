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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * SSL connector hostname verification tests.
 *
 * @author Petr Bouda
 */
@RunWith(Parameterized.class)
public class SslConnectorHostnameVerifierTest extends AbstractConnectorServerTest {

    private static final String CLIENT_TRUST_STORE = "truststore-example_com-client";
    private static final String SERVER_KEY_STORE = "keystore-example_com-server";

    @Override
    protected String serverKeyStore() {
        return SERVER_KEY_STORE;
    }

    @Override
    protected String clientTrustStore() {
        return CLIENT_TRUST_STORE;
    }

    /**
     * Test to apply {@link HostnameVerifier} along with SSL in the predefined connectors
     *
     * @throws Exception in case of a test failure.
     */
    @Test
    public void testHostnameVerifierApplied() throws Exception {
        // Grizzly and Jetty connectors don't support Hostname Verification
        if (isExcluded(Arrays.asList(GrizzlyConnectorProvider.class, JettyConnectorProvider.class))) {
            return;
        }

        final Client client = ClientBuilder.newBuilder()
                .withConfig(new ClientConfig().connectorProvider(connectorProvider))
                .register(HttpAuthenticationFeature.basic("user", "password"))
                .hostnameVerifier(new CustomHostnameVerifier())
                .sslContext(getSslContext())
                .build();

        try {
            client.target(Server.BASE_URI).request().get(Response.class);
            fail("HostnameVerifier was not applied.");
        } catch (ProcessingException pex) {
            CustomHostnameVerifier.HostnameVerifierException hve = getHVE(pex);

            if (hve != null) {
                assertEquals(CustomHostnameVerifier.EX_VERIFIER_MESSAGE, hve.getMessage());
            } else {
                fail("Invalid wrapped exception.");
            }
        }
    }

    private boolean isExcluded(List<Class<? extends ConnectorProvider>> excluded) {
        for (Class<?> clazz : excluded) {
            if (clazz.isAssignableFrom(connectorProvider.getClass())) {
                return true;
            }
        }

        return false;
    }

    private static CustomHostnameVerifier.HostnameVerifierException getHVE(final Throwable stacktrace) {
        Throwable temp = stacktrace;
        do {
            temp = temp.getCause();
            if (temp instanceof CustomHostnameVerifier.HostnameVerifierException) {
                return (CustomHostnameVerifier.HostnameVerifierException) temp;
            }
        } while (temp != null);
        return null;
    }

    public static class CustomHostnameVerifier implements HostnameVerifier {

        private static final String EX_VERIFIER_MESSAGE = "Verifier Applied";

        @Override
        public boolean verify(final String s, final SSLSession sslSession) {
            throw new HostnameVerifierException(EX_VERIFIER_MESSAGE);
        }

        @Override
        public final String toString() {
            return "CUSTOM_HOST_VERIFIER";
        }

        public static class HostnameVerifierException extends RuntimeException {

            public HostnameVerifierException(final String message) {
                super(message);
            }
        }
    }
}
