/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

/**
 * Fake message aggregator used for testing purposes.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public abstract class AbstractTestAggregator implements DataAggregator {
    private static final Logger LOGGER = Logger.getLogger(AbstractTestAggregator.class.getName());
    private static final String[] MESSAGES = new String[] {
            "Where do your RESTful Web Services want to go today?",
            "Jersey RESTful Web Services framework rocks!",
            "Jersey and JAX-RS are cool!",
            "What are the 5 insane but true things about JAX-RS?",
            "Wow, JAX-RS 2.0 provides asynchronous service and client APIs!",
            "Finally! JAX-RS 2.0 adds filters and interceptors support.",
            "Jersey 2.0 programmatic resource API looks great!",
            "How could I live without Jersey ResourceConfig class??",
            "Just wrote my first JAX-RS service using Jersey.",
            "Jersey is the best RESTful framework ever.",
            "JAX-RS rules the web services.",
            "Jersey 2.0 is the new American idol!"
    };
    private static final String IMG_URI
            = "http://files.softicons.com/download/internet-cons/halloween-avatars-icons-by-deleket/png/48/Voodoo%20Doll.png";

    private final String rgbColor;
    private volatile boolean running;

    AbstractTestAggregator(String rgbColor) {
        this.rgbColor = rgbColor;
    }

    @Override
    public void start(final String keywords, final DataListener msgListener) {
        msgListener.onStart();
        running = true;

        final Random rnd = new Random();
        final String aggregatorPrefix = getPrefix();

        Executors.newSingleThreadExecutor().submit(() -> {
            final Client resourceClient = ClientBuilder.newClient();
            resourceClient.register(new MoxyJsonFeature());
            final WebTarget messageStreamResource = resourceClient.target(App.getApiUri()).path(getPath());

            try {
                while (running) {
                    final Message message = new Message(
                            aggregatorPrefix + " " + MESSAGES[rnd.nextInt(MESSAGES.length)],
                            rgbColor,
                            IMG_URI);
                    msgListener.onMessage(message);
                    final Response r = messageStreamResource.request().put(Entity.json(message));
                    if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                        LOGGER.warning("Unexpected PUT message response status code: " + r.getStatus());
                    }
                    Thread.sleep(rnd.nextInt(1000) + 750);
                }
                msgListener.onComplete();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Waiting for a message has been interrupted.", t);
                msgListener.onError();
            }
        });
    }


    @Override
    public void stop() {
        running = false;
    }

    /**
     * Get relative path to the event stream.
     */
    protected abstract String getPath();

    /**
     * Get message prefix to identify the concrete aggregator.
     *
     * @return message prefix (aggregator qualifier)
     */
    protected abstract String getPrefix();

}
