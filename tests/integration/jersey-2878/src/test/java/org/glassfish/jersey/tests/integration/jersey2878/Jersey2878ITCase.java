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

package org.glassfish.jersey.tests.integration.jersey2878;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.fail;

public class Jersey2878ITCase extends JerseyTest {

    private List<InputStream> responseInputStreams = new ArrayList<>();

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        ClientResponseFilter trackInputStreams = new ClientResponseFilter() {
            @Override
            public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
                responseInputStreams.add(responseContext.getEntityStream());
            }
        };

        config.register(trackInputStreams);
    }

    private static void consumeStreamFully(InputStream inputStream) throws IOException {
        while (inputStream.read() != -1) {
            //consume the stream fully
        }
    }

    @Test
    public void thisShouldWorkButFails() throws Exception {
        InputStream stream = target("string").request().get(InputStream.class);
        try {
            consumeStreamFully(stream);
        } finally {
            stream.close();
        }

        try {
            stream.read();
            fail("Exception was not thrown when read() was called on closed stream! Stream implementation: " + stream.getClass());
        } catch (IOException e) {
            // this is desired
        }

        assertThatAllInputStreamsAreClosed();
    }

    @Test
    public void thisWorksButIsReallyUgly() throws Exception {
        Response response = target("string").request().get();
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException("We have to manually check that the response was successful");
        }
        InputStream stream = response.readEntity(InputStream.class);
        try {
            consumeStreamFully(stream);
        } finally {
            response.close();
        }

        try {
            stream.read();
            fail("Exception was not thrown when read() was called on closed stream! Stream implementation: " + stream.getClass());
        } catch (IOException e) {
            // this is desired
        }

        assertThatAllInputStreamsAreClosed();
    }

    @Test
    public void thisAlsoFails() throws Exception {
        Response response = target("string").request().get();
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException("We have to manually check that the response was successful");
        }

        InputStream stream = response.readEntity(InputStream.class);
        try {
            consumeStreamFully(stream);
        } finally {
            stream.close();
        }

        try {
            stream.read();
            fail("Exception was not thrown when read() was called on closed stream! Stream implementation: " + stream.getClass());
        } catch (IOException e) {
            // this is desired
        }

        assertThatAllInputStreamsAreClosed();
    }

    @Test
    public void worksWithACast_ifYouKnowThatYouCanCast() throws Exception {
        Response response = target("string").request().get();
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException("We have to manually check that the response was successful");
        }

        InputStream stream = (InputStream) response.getEntity();
        try {
            consumeStreamFully(stream);
        } finally {
            stream.close();
        }

        try {
            stream.read();
            fail("Exception was not thrown when read() was called on closed stream! Stream implementation: " + stream.getClass());
        } catch (IOException e) {
            // this is desired
        }

        assertThatAllInputStreamsAreClosed();
    }

    private void assertThatAllInputStreamsAreClosed() {
        if (responseInputStreams.size() == 0) {
            fail("no input stream to check");
        }
        for (InputStream stream : responseInputStreams) {
            assertClosed(stream);
        }
    }

    private void assertClosed(InputStream stream) {
        try {
            byte[] buffer = new byte[256];
            stream.read(buffer); //it's not ignored — we're checking for the exception
            fail("Stream is not closed! Stream implementation: " + stream.getClass());
        } catch (IOException e) {
            // an exception is desired
        }
    }
}
