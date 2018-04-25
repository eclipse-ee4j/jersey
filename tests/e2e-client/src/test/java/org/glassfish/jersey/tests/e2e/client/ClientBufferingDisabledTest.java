/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests possibility of disabling buffering of outgoing entity in
 * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider}.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@RunWith(ConcurrentRunner.class)
public class ClientBufferingDisabledTest extends JerseyTest {

    private static final long LENGTH = 200000000L;
    private static final int CHUNK = 2048;
    private static CountDownLatch postLatch = new CountDownLatch(1);

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    @Path("resource")
    public static class MyResource {

        @POST
        public long post(InputStream is) throws IOException {
            int b;
            long count = 0;
            boolean firstByte = true;
            while ((b = is.read()) != -1) {
                if (firstByte) {
                    firstByte = false;
                    postLatch.countDown();
                }

                count++;
            }
            return count;
        }
    }


    /**
     * Test that buffering can be disabled with {@link HttpURLConnection}. By default, the
     * {@code HttpURLConnection} buffers the output entity in order to calculate the
     * Content-length request attribute. This cause problems for large entities.
     * <p>
     * This test uses {@link HttpUrlConnectorProvider#USE_FIXED_LENGTH_STREAMING} to enable
     * fix length streaming on {@code HttpURLConnection}.
     */
    @Test
    public void testDisableBufferingWithFixedLengthViaProperty() {
        postLatch = new CountDownLatch(1);

        // This IS sends out 10 chunks and waits whether they were received on the server. This tests
        // whether the buffering is disabled.
        InputStream is = getInputStream();

        final HttpUrlConnectorProvider connectorProvider = new HttpUrlConnectorProvider();
        ClientConfig clientConfig = new ClientConfig().connectorProvider(connectorProvider);
        clientConfig.property(HttpUrlConnectorProvider.USE_FIXED_LENGTH_STREAMING, true);
        Client client = ClientBuilder.newClient(clientConfig);
        final Response response
                = client.target(getBaseUri()).path("resource")
                .request().header(HttpHeaders.CONTENT_LENGTH, LENGTH).post(
                        Entity.entity(is, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(200, response.getStatus());
        final long count = response.readEntity(long.class);
        Assert.assertEquals("Unexpected content length received.", LENGTH, count);
    }

    /**
     * Test that buffering can be disabled with {@link HttpURLConnection}. By default, the
     * {@code HttpURLConnection} buffers the output entity in order to calculate the
     * Content-length request attribute. This cause problems for large entities.
     * <p>
     * This test uses {@link HttpUrlConnectorProvider#useFixedLengthStreaming()} to enable
     * fix length streaming on {@code HttpURLConnection}.
     */
    @Test
    public void testDisableBufferingWithFixedLengthViaMethod() {
        postLatch = new CountDownLatch(1);

        // This IS sends out 10 chunks and waits whether they were received on the server. This tests
        // whether the buffering is disabled.
        InputStream is = getInputStream();

        final HttpUrlConnectorProvider connectorProvider = new HttpUrlConnectorProvider()
                .useFixedLengthStreaming();
        ClientConfig clientConfig = new ClientConfig().connectorProvider(connectorProvider);
        Client client = ClientBuilder.newClient(clientConfig);
        final Response response
                = client.target(getBaseUri()).path("resource")
                .request().header(HttpHeaders.CONTENT_LENGTH, LENGTH).post(
                        Entity.entity(is, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(200, response.getStatus());
        final long count = response.readEntity(long.class);
        Assert.assertEquals("Unexpected content length received.", LENGTH, count);
    }

    /**
     * Test that buffering can be disabled with {@link HttpURLConnection}. By default, the
     * {@code HttpURLConnection} buffers the output entity in order to calculate the
     * Content-length request attribute. This cause problems for large entities.
     * <p>
     * In Jersey 1.x chunk encoding with {@code HttpURLConnection} was causing bugs
     * which occurred from time to time. This looks to be a case also in Jersey 2.x. This test
     * has failed unpredictably on some machines. Therefore it is disabled now.
     * </p>
     */
    @Test
    @Ignore("fails unpredictable (see javadoc)")
    public void testDisableBufferingWithChunkEncoding() {
        postLatch = new CountDownLatch(1);

        // This IS sends out 10 chunks and waits whether they were received on the server. This tests
        // whether the buffering is disabled.
        InputStream is = getInputStream();

        final HttpUrlConnectorProvider connectorProvider = new HttpUrlConnectorProvider()
                .chunkSize(CHUNK);
        ClientConfig clientConfig = new ClientConfig()
                .connectorProvider(connectorProvider);
        Client client = ClientBuilder.newClient(clientConfig);
        final Response response
                = client.target(getBaseUri()).path("resource")
                .request().post(Entity.entity(is, MediaType.APPLICATION_OCTET_STREAM));
        Assert.assertEquals(200, response.getStatus());
        final long count = response.readEntity(long.class);
        Assert.assertEquals("Unexpected content length received.", LENGTH, count);
    }

    private InputStream getInputStream() {
        return new InputStream() {
            private int cnt = 0;

            @Override
            public int read() throws IOException {
                cnt++;
                if (cnt > CHUNK * 10) {
                    try {
                        postLatch.await(3 * getAsyncTimeoutMultiplier(), TimeUnit.SECONDS);
                        Assert.assertEquals("waiting for chunk on the server side time-outed", 0, postLatch.getCount());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (cnt <= LENGTH) {
                    return 'a';
                } else {
                    return -1;
                }
            }
        };
    }
}
