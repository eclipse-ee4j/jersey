/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import javax.annotation.PreDestroy;

import org.glassfish.jersey.client.ClientLifecycleListener;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

/**
 * Assert that pre destroy method on providers is invoked.
 *
 * @author Michal Gajdos
 */
public class ClientDestroyTest extends JerseyTest {

    private static final Map<String, Boolean> destroyed = new HashMap<>();

    @Override
    @Before
    public void setUp() throws Exception {
        destroyed.clear();
        destroyed.put("filter", false);
        destroyed.put("reader", false);
        destroyed.put("feature", false);

        super.setUp();
    }

    @Path("/")
    public static class Resource {

        @GET
        public String get(@HeaderParam("foo") final String foo) {
            return "resource-" + foo;
        }
    }

    public static class MyFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("foo", "bar");
        }

        @PreDestroy
        public void preDestroy() {
            destroyed.put("filter", true);
        }
    }

    public static class MyReader implements ReaderInterceptor {

        @Override
        public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
            final Object entity = context.proceed();
            return "reader-" + entity;
        }

        @PreDestroy
        public void preDestroy() {
            destroyed.put("reader", true);
        }
    }

    public static class MyFeature implements Feature {

        @PreDestroy
        public void preDestroy() {
            destroyed.put("feature", true);
        }

        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    @Test
    public void testClientInvokePreDestroyMethodOnProviderClass() throws Exception {
        final Client client = ClientBuilder.newClient()
                .register(MyFilter.class)
                .register(MyReader.class)
                .register(MyFeature.class);

        assertThat(client.target(getBaseUri()).request().get(String.class), is("reader-resource-bar"));

        checkDestroyed(false);
        client.close();
        checkDestroyed(true);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    private void checkDestroyed(final boolean shouldBeDestroyed) {
        for (final Map.Entry<String, Boolean> entry : destroyed.entrySet()) {
            assertThat(entry.getKey() +  " should" + (shouldBeDestroyed ? "" : " not") + " be destroyed",
                    entry.getValue(), is(shouldBeDestroyed));
        }
    }

    public static class FooListener implements ClientRequestFilter, ClientLifecycleListener {
        public static volatile boolean INITIALIZED = false;
        public static volatile boolean CLOSED = false;

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException { /* do nothing */ }

        @Override
        public void onClose() {
            CLOSED = true;
        }

        @Override
        public void onInit() {
            INITIALIZED = true;
        }

        // to check if closing works also for class-registered providers
        public static boolean isClosed() {
            return CLOSED;
        }

        public static boolean isInitialized() {
            return INITIALIZED;
        }
    }

    public static class BarListener implements ClientRequestFilter, ClientLifecycleListener {
        protected volatile boolean closedByClientClose = false;
        protected volatile boolean initialized = false;
        protected volatile boolean closedByFinalize = false;

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException { /* do nothing */ }

        @Override
        public void onInit() {
            this.initialized = true;
        }

        @Override
        public synchronized void onClose() {
            // There is the ClientRuntime created twice, each of which has filter "filterOnClient" registered.
            // onClose can be called from Client#close or from ClientRuntime#finalize
            if (ClientDestroyTest.isCalledFromFinalizer()) {
                this.closedByFinalize = true;
            } else {
                closedByClientClose = true;
            }
        }

        public boolean isClosedByClientClose() {
            return closedByClientClose;
        }

        public boolean isClosedByFinalize() {
            return closedByFinalize;
        }

        public boolean isClosed() {
            return closedByClientClose || closedByFinalize;
        }

        public boolean isInitialized() {
            return initialized;
        }
    }

    // another type needed, as multiple registrations of the same type are not allowed
    public static class BarListener2 extends BarListener {
    }

    @Test
    public void testLifecycleListenerProvider() {
        final JerseyClientBuilder builder = new JerseyClientBuilder();
        final JerseyClient client = builder.build();

        final BarListener filterOnClient = new BarListener();
        final BarListener filterOnTarget = new BarListener2();

        // ClientRuntime initializes lazily, so it is forced by invoking a (dummy) request
        client.register(filterOnTarget);                                                   // instance registered into client
        client.target(getBaseUri()).register(filterOnClient).request().get(String.class);   // instance registration into target

        assertTrue("Filter registered on Client was expected to be already initialized.", filterOnClient.isInitialized());
        assertTrue("Filter registered on Target was expected to be already initialized.", filterOnTarget.isInitialized());

        client.target(getBaseUri()).register(FooListener.class).request().get(String.class); // class registration into target

        assertTrue("Class-registered filter was expected to be already initialized", FooListener.isInitialized());

        assertFalse("Class-registered filter was expected to be still open.", FooListener.isClosed());
        assertFalse("Filter registered on Client was expected to be still open.", filterOnClient.isClosedByClientClose());
        assertFalse("Filter registered on Target was expected to be still open.", filterOnTarget.isClosedByClientClose());

        client.close();

        assertTrue("Class-registered filter was expected to be closed.", FooListener.isClosed());
        assertTrue("Filter registered on Client was expected to be closed.", filterOnClient.isClosed());
        assertTrue("Filter registered on Target was expected to be closed.", filterOnTarget.isClosed());
    }

    private static boolean isCalledFromFinalizer() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            if ("finalize".equals(element.getMethodName())) {
                return true;
            }
        }
        return false;
    }

}
