/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

/**
 * Twitter message-based data aggregator implementation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class TwitterAggregator implements DataAggregator {
    private static final Logger LOGGER = Logger.getLogger(TwitterAggregator.class.getName());

    private volatile boolean cancelled;
    private final String rgbColor;

    /**
     * Create new twitter message aggregator with a specific message color.
     *
     * @param rgbColor message color.
     */
    public TwitterAggregator(String rgbColor) {
        this.rgbColor = rgbColor;
    }

    @Override
    public void start(final String keywords, final DataListener msgListener) {
        cancelled = false;

//        System.setProperty("http.proxyHost", "www-proxy.us.oracle.com");
//        System.setProperty("http.proxyPort", "80");
//        System.setProperty("https.proxyHost", "www-proxy.us.oracle.com");
//        System.setProperty("https.proxyPort", "80");


        final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();

        final Future<?> readerHandle = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                SslConfigurator sslConfig = SslConfigurator.newInstance()
                        .trustStoreFile("./truststore_client")
                        .trustStorePassword("asdfgh")

                        .keyStoreFile("./keystore_client")
                        .keyPassword("asdfgh");

                final Client client = ClientBuilder.newBuilder().sslContext(sslConfig.createSSLContext()).build();
                client.property(ClientProperties.CONNECT_TIMEOUT, 2000)
                        .register(new MoxyJsonFeature())
                        .register(HttpAuthenticationFeature.basic(App.getTwitterUserName(), App.getTwitterUserPassword()))
                        .register(GZipEncoder.class);

                final Response response = client.target("https://stream.twitter.com/1.1/statuses/filter.json")
                        .queryParam("track", keywords)
//                .queryParam("locations", "-122.75,36.8,-121.75,37.8") // San Francisco
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header(HttpHeaders.HOST, "stream.twitter.com")
                        .header(HttpHeaders.USER_AGENT, "Jersey/2.0")
                        .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                        .get();

                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    LOGGER.log(Level.WARNING, "Error connecting to Twitter Streaming API: " + response.getStatus());
                    msgListener.onError();
                    return;
                }
                msgListener.onStart();

                try {
                    final ChunkedInput<Message> chunks = response.readEntity(new GenericType<ChunkedInput<Message>>() {
                    });
                    try {
                        while (!Thread.interrupted()) {
                            Message message = chunks.read();
                            if (message == null) {
                                break;
                            }
                            try {
                                message.setRgbColor(rgbColor);
                                System.out.println(message.toString());
                                messages.put(message);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    } finally {
                        if (chunks != null) {
                            chunks.close();
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Reading from the Twitter stream has failed", t);
                    messages.offer(null);
                    msgListener.onError();
                }
            }
        });

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                final Client resourceClient = ClientBuilder.newClient();
                resourceClient.register(new MoxyJsonFeature());
                final WebTarget messageStreamResource = resourceClient.target(App.getApiUri()).path("message/stream");

                Message message = null;
                try {
                    while (!cancelled && (message = messages.take()) != null) {
                        msgListener.onMessage(message);

                        final Response r = messageStreamResource.request().put(Entity.json(message));
                        if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                            LOGGER.warning("Unexpected PUT message response status code: " + r.getStatus());
                        }
                    }

                    if (message == null) {
                        LOGGER.info("Timed out while waiting for a message.");
                    }
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "Waiting for a message has been interrupted.", ex);
                } finally {
                    readerHandle.cancel(true);
                    msgListener.onComplete();
                }
            }
        });
    }

    @Override
    public void stop() {
        cancelled = true;
    }
}
