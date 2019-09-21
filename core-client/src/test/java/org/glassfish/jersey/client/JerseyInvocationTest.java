/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Martin Matula
 */
public class JerseyInvocationTest {

    /**
     * Regression test for JERSEY-1257
     */
    @Test
    public void testOverrideHeadersWithMap() {
        final MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
        map.add("a-header", "b-header");
        final JerseyInvocation invocation = buildInvocationWithHeaders(map);
        assertEquals(1, invocation.request().getHeaders().size());
        assertEquals("b-header", invocation.request().getHeaders().getFirst("a-header"));
    }

    /**
     * Regression test for JERSEY-1257
     */
    @Test
    public void testClearHeaders() {
        final JerseyInvocation invocation = buildInvocationWithHeaders(null);
        assertTrue(invocation.request().getHeaders().isEmpty());
    }

    /**
     * Regression test for JERSEY-2562.
     */
    @Test
    public void testClearHeader() {
        final Client client = ClientBuilder.newClient();
        final Invocation.Builder builder = client.target("http://localhost:8080/mypath").request();
        final JerseyInvocation invocation = (JerseyInvocation) builder
                .header("foo", "bar").header("foo", null).header("bar", "foo")
                .buildGet();
        final MultivaluedMap<String, Object> headers = invocation.request().getHeaders();

        assertThat(headers.size(), is(1));
        assertThat(headers.keySet(), hasItem("bar"));
    }

    private JerseyInvocation buildInvocationWithHeaders(final MultivaluedMap<String, Object> headers) {
        final Client c = ClientBuilder.newClient();
        final Invocation.Builder builder = c.target("http://localhost:8080/mypath").request();
        return (JerseyInvocation) builder.header("unexpected-header", "unexpected-header").headers(headers).buildGet();
    }

    /**
     * Checks that presence of request entity fo HTTP DELETE method does not fail in Jersey.
     * Instead, the request is propagated up to HttpURLConnection, where it fails with
     * {@code ProtocolException}.
     * <p/>
     * See also JERSEY-1711.
     *
     * @see #overrideHttpMethodBasedComplianceCheckNegativeTest()
     */
    @Test
    public void overrideHttpMethodBasedComplianceCheckTest() {
        final Client c1 = ClientBuilder.newClient().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        try {
            c1.target("http://localhost:8080/myPath").request().method("DELETE", Entity.text("body"));
            fail("ProcessingException expected.");
        } catch (final ProcessingException ex) {
            assertThat(ex.getCause().getClass(), anyOf(CoreMatchers.<Class<?>>equalTo(ProtocolException.class),
                    CoreMatchers.<Class<?>>equalTo(ConnectException.class)));
        }

        final Client c2 = ClientBuilder.newClient();
        try {
            c2.target("http://localhost:8080/myPath").request().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION,
                    true).method("DELETE", Entity.text("body"));
            fail("ProcessingException expected.");
        } catch (final ProcessingException ex) {
            assertThat(ex.getCause().getClass(), anyOf(CoreMatchers.<Class<?>>equalTo(ProtocolException.class),
                    CoreMatchers.<Class<?>>equalTo(ConnectException.class)));
        }

        final Client c3 = ClientBuilder.newClient().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, false);
        try {
            c3.target("http://localhost:8080/myPath").request().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION,
                    true).method("DELETE", Entity.text("body"));
            fail("ProcessingException expected.");
        } catch (final ProcessingException ex) {
            assertThat(ex.getCause().getClass(), anyOf(CoreMatchers.<Class<?>>equalTo(ProtocolException.class),
                    CoreMatchers.<Class<?>>equalTo(ConnectException.class)));
        }
    }

    /**
     * Checks that presence of request entity fo HTTP DELETE method fails in Jersey with {@code IllegalStateException}
     * if HTTP spec compliance is not suppressed by {@link ClientProperties#SUPPRESS_HTTP_COMPLIANCE_VALIDATION} property.
     * <p/>
     * See also JERSEY-1711.
     *
     * @see #overrideHttpMethodBasedComplianceCheckTest()
     */
    @Test
    public void overrideHttpMethodBasedComplianceCheckNegativeTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            c1.target("http://localhost:8080/myPath").request().method("DELETE", Entity.text("body"));
            fail("IllegalStateException expected.");
        } catch (final IllegalStateException expected) {
            // pass
        }

        final Client c2 = ClientBuilder.newClient();
        try {
            c2.target("http://localhost:8080/myPath").request().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION,
                    false).method("DELETE", Entity.text("body"));
            fail("IllegalStateException expected.");
        } catch (final IllegalStateException expected) {
            // pass
        }

        final Client c3 = ClientBuilder.newClient().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        try {
            c3.target("http://localhost:8080/myPath").request().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION,
                    false).method("DELETE", Entity.text("body"));
            fail("IllegalStateException expected.");
        } catch (final IllegalStateException expected) {
            // pass
        }
    }

    @Test
    public void testNullResponseType() throws Exception {
        final Client client = ClientBuilder.newClient();
        client.register(new ClientRequestFilter() {
            @Override
            public void filter(final ClientRequestContext requestContext) throws IOException {
                requestContext.abortWith(Response.ok().build());
            }
        });

        final WebTarget target = client.target("http://localhost:8080/mypath");

        final Class<Response> responseType = null;
        final String[] methods = new String[] {"GET", "PUT", "POST", "DELETE", "OPTIONS"};

        for (final String method : methods) {
            final Invocation.Builder request = target.request();

            try {
                request.method(method, responseType);
                fail("IllegalArgumentException expected.");
            } catch (final IllegalArgumentException iae) {
                // OK.
            }

            final Invocation build = "PUT".equals(method) ? request.build(method, Entity.entity("",
                    MediaType.TEXT_PLAIN_TYPE)) : request.build(method);

            try {
                build.submit(responseType);
                fail("IllegalArgumentException expected.");
            } catch (final IllegalArgumentException iae) {
                // OK.
            }

            try {
                build.invoke(responseType);
                fail("IllegalArgumentException expected.");
            } catch (final IllegalArgumentException iae) {
                // OK.
            }

            try {
                request.async().method(method, responseType);
                fail("IllegalArgumentException expected.");
            } catch (final IllegalArgumentException iae) {
                // OK.
            }
        }
    }

    @Test
    public void failedCallbackTest() throws InterruptedException {
        final Invocation.Builder builder = ClientBuilder.newClient().target("http://localhost:888/").request();
        for (int i = 0; i < 1; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicInteger ai = new AtomicInteger(0);
            final InvocationCallback<String> callback = new InvocationCallback<String>() {
                @Override
                public void completed(final String arg0) {
                    try {
                        ai.set(ai.get() + 1);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void failed(final Throwable throwable) {
                    try {

                        int result = 10;
                        if (throwable instanceof ProcessingException) {
                            result += 100;
                        }
                        final Throwable ioe = throwable.getCause();
                        if (ioe instanceof IOException) {
                            result += 1000;
                        }
                        ai.set(ai.get() + result);
                    } finally {
                        latch.countDown();
                    }
                }
            };

            final Invocation invocation = builder.buildGet();
            final Future<String> future = invocation.submit(callback);
            try {
                future.get();
                fail("future.get() should have failed.");
            } catch (final ExecutionException e) {
                final Throwable pe = e.getCause();
                assertTrue("Execution exception cause is not a ProcessingException: " + pe.toString(),
                        pe instanceof ProcessingException);
                final Throwable ioe = pe.getCause();
                assertTrue("Execution exception cause is not an IOException: " + ioe.toString(), ioe instanceof IOException);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }

            latch.await(1, TimeUnit.SECONDS);

            assertEquals(1110, ai.get());
        }
    }

    public static class MyUnboundCallback<V> implements InvocationCallback<V> {

        private final CountDownLatch latch;
        private volatile Throwable throwable;

        public MyUnboundCallback(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void completed(final V v) {
            latch.countDown();
        }

        @Override
        public void failed(final Throwable throwable) {
            this.throwable = throwable;
            latch.countDown();
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    @Test
    public void failedUnboundGenericCallback() throws InterruptedException {
        final Invocation invocation = ClientBuilder.newClient().target("http://localhost:888/").request().buildGet();
        final CountDownLatch latch = new CountDownLatch(1);

        final MyUnboundCallback<String> callback = new MyUnboundCallback<String>(latch);
        invocation.submit(callback);

        latch.await(1, TimeUnit.SECONDS);

        assertThat(callback.getThrowable(), CoreMatchers.instanceOf(ProcessingException.class));
        assertThat(callback.getThrowable().getCause(), CoreMatchers.instanceOf(IllegalArgumentException.class));
        assertThat(callback.getThrowable().getCause().getMessage(), CoreMatchers
                .allOf(CoreMatchers.containsString(MyUnboundCallback.class.getName()),
                        CoreMatchers.containsString(InvocationCallback.class.getName())));
    }

    @Test
    public void testSubmitWithGenericType() throws Exception {
        _submitWithGenericType(new GenericType<String>() {});
    }

    @Test
    public void testSubmitWithGenericTypeParam() throws Exception {
        _submitWithGenericType(new GenericType(String.class) {});
    }

    private void _submitWithGenericType(final GenericType type) throws Exception {
        final Invocation.Builder builder = ClientBuilder.newClient()
                .register(TerminatingFilter.class)
                .target("http://localhost/")
                .request();

        final AtomicReference<String> reference = new AtomicReference<String>();
        final CountDownLatch latch = new CountDownLatch(1);

        final InvocationCallback callback = new InvocationCallback<Object>() {
            @Override
            public void completed(final Object obj) {
                reference.set(obj.toString());
                latch.countDown();
            }

            @Override
            public void failed(final Throwable throwable) {
                latch.countDown();
            }
        };

        //noinspection unchecked
        ((JerseyInvocation) builder.buildGet())
                .submit(type, (InvocationCallback<String>) callback);

        latch.await();

        assertThat(reference.get(), is("ENTITY"));
    }

    public static class TerminatingFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok("ENTITY").build());
        }
    }

    @Test
    public void runtimeExceptionInAsyncInvocation() throws ExecutionException, InterruptedException {
        final AsyncInvoker ai = ClientBuilder.newClient().register(new ExceptionInvokerFilter())
                .target("http://localhost:888/").request().async();

        try {
            ai.get().get();
            fail("ExecutionException should be thrown");
        } catch (ExecutionException ee) {
            assertEquals(ProcessingException.class, ee.getCause().getClass());
            assertEquals(RuntimeException.class, ee.getCause().getCause().getClass());
        }
    }

    public static class ExceptionInvokerFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            throw new RuntimeException("ExceptionInvokerFilter RuntimeException");
        }
    }
}
