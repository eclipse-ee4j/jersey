/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * The LargeDataTest reproduces a problem when bytes of large data sent are incorrectly sent.
 * As a result, the request body is different than what was sent by the client.
 * <p>
 * In order to be able to inspect the request body, the generated data is a sequence of numbers
 * delimited with new lines. Such as
 * <pre><code>
 *     1
 *     2
 *     3
 *
 *     ...
 *
 *     57234
 *     57235
 *     57236
 *
 *     ...
 * </code></pre>
 * It is also possible to send the data to netcat: {@code nc -l 8080} and verify the problem is
 * on the client side.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class LargeDataTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(LargeDataTest.class.getName());
    private static final int LONG_DATA_SIZE = 1_000_000;  // for large set around 5GB, try e.g.: 536_870_912;
    private static volatile Throwable exception;

    private static StreamingOutput longData(long sequence) {
        return out -> {
            long offset = 0;
            while (offset < sequence) {
                out.write(Long.toString(offset).getBytes());
                out.write('\n');
                offset++;
            }
        };
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig(HttpMethodResource.class);
        config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.HEADERS_ONLY));
        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new ApacheConnectorProvider());
    }

    @Test
    public void postWithLargeData() throws Throwable {
        WebTarget webTarget = target("test");

        Response response = webTarget.request().post(Entity.entity(longData(LONG_DATA_SIZE), MediaType.TEXT_PLAIN_TYPE));

        try {
            if (exception != null) {

                // the reason to throw the exception is that IntelliJ gives you an option to compare the expected with the actual
                throw exception;
            }

            Assert.assertEquals("Unexpected error: " + response.getStatus(),
                    Status.Family.SUCCESSFUL,
                    response.getStatusInfo().getFamily());
        } finally {
            response.close();
        }
    }

    @Path("/test")
    public static class HttpMethodResource {

        @POST
        public Response post(InputStream content) {
            try {

                longData(LONG_DATA_SIZE).write(new OutputStream() {

                    private long position = 0;
//                    private long mbRead = 0;

                    @Override
                    public void write(final int generated) throws IOException {
                        int received = content.read();

                        if (received != generated) {
                            throw new IOException("Bytes don't match at position " + position
                                    + ": received=" + received
                                    + ", generated=" + generated);
                        }

                        position++;
//                        if (position % (1024 * 1024) == 0) {
//                            mbRead++;
//                            System.out.println("MB read: " + mbRead);
//                        }
                    }
                });
            } catch (IOException e) {
                exception = e;
                throw new ServerErrorException(e.getMessage(), 500, e);
            }

            return Response.ok().build();
        }

    }
}
