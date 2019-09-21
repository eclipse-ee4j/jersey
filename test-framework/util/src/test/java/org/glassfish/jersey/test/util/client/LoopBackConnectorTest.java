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

package org.glassfish.jersey.test.util.client;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Basic {@link org.glassfish.jersey.test.util.client.LoopBackConnector} unit tests.
 *
 * @author Michal Gajdos
 */
public class LoopBackConnectorTest {

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient(LoopBackConnectorProvider.getClientConfig());
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void testHeadersAndStatus() throws Exception {
        final Response response = client.target("baz").request()
                .header("foo", "bar")
                .header("bar", "foo")
                .get();

        assertThat("Unexpected HTTP response status", response.getStatus(), is(LoopBackConnector.TEST_LOOPBACK_CODE));
        assertThat("Invalid value of header 'foo'", response.getHeaderString("foo"), is("bar"));
        assertThat("Invalid value of header 'bar'", response.getHeaderString("bar"), is("foo"));
    }

    @Test
    public void testEntity() throws Exception {
        final Response response = client.target("baz").request().post(Entity.text("foo"));

        assertThat("Invalid entity received", response.readEntity(String.class), is("foo"));
    }

    @Test
    public void testEntityMediaType() throws Exception {
        final Response response = client.target("baz").request().post(Entity.entity("foo", "foo/bar"));

        assertThat("Invalid entity received", response.readEntity(String.class), is("foo"));
        assertThat("Invalid content-type received", response.getMediaType(), is(new MediaType("foo", "bar")));
    }

    @Test(expected = IllegalStateException.class)
    public void testClose() throws Exception {
        client.close();
        client.target("baz").request().get();
    }

    @Test
    public void testAsync() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> throwable = new AtomicReference<>();

        client.target("baz").request().async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(final Response response) {
                latch.countDown();
            }

            @Override
            public void failed(final Throwable t) {
                throwable.set(t);
                latch.countDown();
            }
        });

        latch.await();

        assertThat("Async request failed", throwable.get(), nullValue());
    }

    @Test(expected = ProcessingException.class)
    public void testInvalidEntity() throws Exception {
        client.target("baz").request().post(Entity.json(Arrays.asList("foo", "bar")));
    }
}
