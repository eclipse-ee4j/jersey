/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async.managed;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.server.ManagedAsync;

/**
 * Example of a simple fire&forget point-to-point messaging resource.
 *
 * This version of the messaging resource does not block when POSTing a new message.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("chat")
@Produces("application/json")
public class ChatResource {

    private static final BlockingQueue<AsyncResponse> suspended = new ArrayBlockingQueue<AsyncResponse>(5);

    @GET
    @ManagedAsync
    public void getMessage(@Suspended final AsyncResponse ar) throws InterruptedException {
        suspended.put(ar);
    }

    @POST
    @ManagedAsync
    public String postMessage(final Message message) throws InterruptedException {
        final AsyncResponse asyncResponse = suspended.take();
        asyncResponse.resume(message);
        return "Sent!";
    }
}
