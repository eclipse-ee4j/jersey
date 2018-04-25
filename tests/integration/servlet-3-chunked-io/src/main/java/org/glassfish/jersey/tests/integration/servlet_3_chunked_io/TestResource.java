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

package org.glassfish.jersey.tests.integration.servlet_3_chunked_io;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ChunkedOutput;

/**
 * Test resource.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("/test")
public class TestResource {

    private static final Logger LOGGER = Logger.getLogger(TestResource.class.getName());

    /**
     * Get chunk stream of JSON data - from JSON POJOs.
     *
     * @return chunk stream.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("from-pojo")
    public ChunkedOutput<Message> getFromPojo() {
        final ChunkedOutput<Message> output = new ChunkedOutput<>(Message.class, "\r\n");

        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        output.write(new Message(i, "test"));
                        Thread.sleep(200);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Error writing chunk.", e);
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Sleep interrupted.", e);
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        output.close();
                    } catch (final IOException e) {
                        LOGGER.log(Level.INFO, "Error closing chunked output.", e);
                    }
                }
            }
        }.start();

        return output;
    }

    /**
     * Get chunk stream of JSON data - from string.
     *
     * @return chunk stream.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("from-string")
    public ChunkedOutput<String> getFromText() {
        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        output.write(new Message(i, "test").toString() + "\r\n");
                        Thread.sleep(200);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Error writing chunk.", e);
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Sleep interrupted.", e);
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        output.close();
                    } catch (final IOException e) {
                        LOGGER.log(Level.INFO, "Error closing chunked output.", e);
                    }
                }
            }
        }.start();

        return output;
    }

    /**
     * {@link org.glassfish.jersey.server.ChunkedOutput#close()} is called before method returns it's entity. Resource reproduces
     * JERSEY-2558 issue.
     *
     * @return (closed) chunk stream.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("close-before-return")
    public ChunkedOutput<Message> closeBeforeReturn() {
        final ChunkedOutput<Message> output = new ChunkedOutput<>(Message.class, "\r\n");
        final CountDownLatch latch = new CountDownLatch(1);

        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        output.write(new Message(i, "test"));
                        Thread.sleep(200);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Error writing chunk.", e);
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Sleep interrupted.", e);
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        output.close();
                        // Worker thread can continue.
                        latch.countDown();
                    } catch (final IOException e) {
                        LOGGER.log(Level.INFO, "Error closing chunked output.", e);
                    }
                }
            }
        }.start();

        try {
            // Wait till new thread closes the chunked output.
            latch.await();
            return output;
        } catch (final InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /**
     * Test combination of AsyncResponse and ChunkedOutput.
     *
     * @param response async response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chunked-async")
    public void closeBeforeReturnAsync(@Suspended final AsyncResponse response) {
        // Set timeout to able to resume but not send the chunks (setting the ChunkedOutput should remove the timeout).
        response.setTimeout(1500, TimeUnit.SECONDS);

        new Thread() {

            @Override
            public void run() {
                final ChunkedOutput<Message> output = new ChunkedOutput<>(Message.class, "\r\n");

                try {
                    // Let the method return.
                    Thread.sleep(1000);
                    // Resume.
                    response.resume(output);
                    // Wait for resume to complete.
                    Thread.sleep(1000);

                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i < 3; i++) {
                                    output.write(new Message(i, "test"));
                                    Thread.sleep(200);
                                }
                            } catch (final IOException e) {
                                LOGGER.log(Level.SEVERE, "Error writing chunk.", e);
                            } catch (final InterruptedException e) {
                                LOGGER.log(Level.SEVERE, "Sleep interrupted.", e);
                                Thread.currentThread().interrupt();
                            } finally {
                                try {
                                    output.close();
                                } catch (final IOException e) {
                                    LOGGER.log(Level.INFO, "Error closing chunked output.", e);
                                }
                            }
                        }
                    }.start();
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Sleep interrupted.", e);
                    Thread.currentThread().interrupt();
                }
            }
        }.start();
    }
}
