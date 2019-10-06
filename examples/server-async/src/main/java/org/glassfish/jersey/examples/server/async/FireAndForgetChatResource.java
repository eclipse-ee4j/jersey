/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;

/**
 * Example of a simple fire&forget point-to-point messaging resource.
 *
 * This version of the messaging resource does not block when POSTing a new message.
 *
 * @author Marek Potociar
 */
@Path(App.ASYNC_MESSAGING_FIRE_N_FORGET_PATH)
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class FireAndForgetChatResource {

    public static final String POST_NOTIFICATION_RESPONSE = "Message sent";
    //
    private static final Logger LOGGER = Logger.getLogger(FireAndForgetChatResource.class.getName());
    private static final Level DEBUG = Level.INFO;
    //
    private static final ExecutorService QUEUE_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("fire&forget-chat-resource-executor-%d")
            .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
            .build());
    private static final BlockingQueue<AsyncResponse> suspended = new ArrayBlockingQueue<AsyncResponse>(5);

    @GET
    public void pickUpMessage(@Suspended final AsyncResponse ar, @QueryParam("id") final String messageId)
            throws InterruptedException {
        LOGGER.log(DEBUG, "Received GET <{0}> with context {1} on thread {2}.",
                new Object[] {messageId, ar.toString(), Thread.currentThread().getName()});
        QUEUE_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    suspended.put(ar);
                    LOGGER.log(DEBUG, "GET <{0}> context {1} scheduled for resume.",
                            new Object[] {messageId, ar.toString()});
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE,
                            "Waiting for a message pick-up interrupted. Cancelling context" + ar.toString(), ex);
                    ar.cancel(); // close the open connection
                }
            }
        });
    }

    @POST
    public String postMessage(final String message) throws InterruptedException {
        LOGGER.log(DEBUG, "Received POSTed message <{0}> on thread {1}.",
                new Object[] {message, Thread.currentThread().getName()});
        QUEUE_EXECUTOR.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    final AsyncResponse suspendedResponse = suspended.take();
                    LOGGER.log(DEBUG, "Resuming GET context {0} with a message <{1}> on thread {2}.",
                            new Object[] {suspendedResponse.toString(), message, Thread.currentThread().getName()});
                    suspendedResponse.resume(message);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE,
                            "Waiting for a sending a message <" + message + "> has been interrupted.", ex);
                }
            }
        });

        return POST_NOTIFICATION_RESPONSE;
    }
}
