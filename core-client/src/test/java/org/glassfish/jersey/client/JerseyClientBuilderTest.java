/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Priority;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link JerseyClient} unit test.
 *
 * @author Marek Potociar
 * @author Michal Gajdos
 */
public class JerseyClientBuilderTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private JerseyClientBuilder builder;

    @Before
    public void setUp() {
        builder = new JerseyClientBuilder();
    }

    @Test
    public void testBuildClientWithNullSslConfig() throws KeyStoreException {
        try {
            builder.sslContext(null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }

        try {
            builder.keyStore(null, "abc");
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }
        try {
            builder.keyStore(null, "abc".toCharArray());
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            builder.keyStore(ks, (String) null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }
        try {
            builder.keyStore(ks, (char[]) null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }

        try {
            builder.keyStore(null, (String) null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }
        try {
            builder.keyStore(null, (char[]) null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }

        try {
            builder.trustStore(null);
            fail("NullPointerException expected for 'null' SSL context.");
        } catch (NullPointerException npe) {
            // pass
        }
    }

    @Test
    public void testOverridingSslConfig() throws KeyStoreException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLContext ctx = SSLContext.getInstance("SSL");
        Client client;

        client = new JerseyClientBuilder().keyStore(ks, "qwerty").sslContext(ctx).build();
        assertSame("SSL context not the same as set in the client builder.", ctx, client.getSslContext());

        client = new JerseyClientBuilder().sslContext(ctx).trustStore(ks).build();
        assertNotSame("SSL context not overridden in the client builder.", ctx, client.getSslContext());
    }

    @Priority(2)
    public static class AbortingClientFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok("ok").build());
        }
    }

    @Priority(1)
    public static class ClientCreatingFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            if (Boolean.valueOf(requestContext.getHeaderString("create"))) {
                assertThat(requestContext.getProperty("foo").toString(), equalTo("rab"));

                final Client client = ClientBuilder.newBuilder().withConfig(requestContext.getConfiguration()).build();
                final Response response = client.target("http://localhost").request().header("create", false).get();

                requestContext.abortWith(response);
            } else {
                assertThat(requestContext.getConfiguration().getProperty("foo").toString(), equalTo("bar"));
            }
        }
    }

    public static class ClientFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            if (context.getConfiguration().isRegistered(AbortingClientFilter.class)) {
                throw new RuntimeException("Already Configured!");
            }

            context.register(ClientCreatingFilter.class);
            context.register(AbortingClientFilter.class);

            context.property("foo", "bar");

            return true;
        }
    }

    @Test
    public void testCreateClientWithConfigFromClient() throws Exception {
        _testCreateClientWithAnotherConfig(false);
    }

    @Test
    public void testCreateClientWithConfigFromRequestContext() throws Exception {
        _testCreateClientWithAnotherConfig(true);
    }


    public void _testCreateClientWithAnotherConfig(final boolean clientInFilter) throws Exception {
        final Client client = ClientBuilder.newBuilder().register(new ClientFeature()).build();
        Response response = client.target("http://localhost")
                .request().property("foo", "rab").header("create", clientInFilter).get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("ok"));

        final Client newClient = ClientBuilder.newClient(client.getConfiguration());
        response = newClient.target("http://localhost")
                .request().property("foo", "rab").header("create", clientInFilter).get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("ok"));

        final Client newClientFromBuilder = ClientBuilder.newBuilder().withConfig(client.getConfiguration()).build();
        response = newClientFromBuilder.target("http://localhost")
                .request().property("foo", "rab").header("create", clientInFilter).get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("ok"));
    }

    @Test
    public void testRegisterIrrelevantContractsMap() {
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        final Map<Class<?>, Integer> contracts = new HashMap<>();
        contracts.put(Object.class, 500);
        contracts.put(String.class, 501);
        int sizeBeforeRegister = contracts.size();
        clientBuilder.register(ClientConfigTest.MyProvider.class, contracts);
        int sizeAfterRegister = contracts.size();

        assertThat(sizeBeforeRegister, equalTo(sizeAfterRegister));
    }

    @Test
    public void testRegisterNullMap() {
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        final Map<Class<?>, Integer> contracts = null;
        clientBuilder.register(ClientConfigTest.MyProvider.class, contracts);

        assertNull(contracts);
    }

    @Test(expected = Test.None.class) //no exception shall be thrown
    public void testRegisterIrrelevantImmutableContractsMap() {
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        final Map<Class<?>, Integer> contracts = new HashMap<>();
        contracts.put(Object.class, 500);
        contracts.put(String.class, 501);
        final Map<Class<?>, Integer> immutableContracts = Collections.unmodifiableMap(contracts);
        int sizeBeforeRegister = immutableContracts.size();
        clientBuilder.register(ClientConfigTest.MyProvider.class, immutableContracts);
        int sizeAfterRegister = immutableContracts.size(); //that just proves everything passed OK
        // otherwise exception already would been thrown

        assertThat(sizeBeforeRegister, equalTo(sizeAfterRegister));
    }

    @Test
    public void testNegativeConnectTimeout() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        exception.expect(IllegalArgumentException.class);
        clientBuilder.connectTimeout(-1, TimeUnit.SECONDS);
    }

    @Test
    public void testNegativeReadTimeout() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        exception.expect(IllegalArgumentException.class);
        clientBuilder.readTimeout(-1, TimeUnit.SECONDS);
    }
}
