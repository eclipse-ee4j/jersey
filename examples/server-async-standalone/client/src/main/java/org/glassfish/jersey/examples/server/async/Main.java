/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;

/**
 * Long-running asynchronous service client.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Main {

    /**
     * Main client entry point.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        System.exit(runClient(args));
    }

    /**
     * Client - business logic.
     *
     * @param args command-line arguments.
     * @return exit code of the utility. {@code 0} if everything completed without errors, {@code -1} otherwise.
     */
    static int runClient(final String[] args) {
        // Parsing command-line arguments
        final Config config = Config.parse(args);
        System.out.println(String.format("\nStarting to execute %d requests:\n", config.requests));
        // Creating JAX-RS client
        final Client client = ClientBuilder.newClient();
        // Targeting echo resource at URI "<baseUri>/long-running/(sync|async)/{echo}"
        final WebTarget echoResource = client.target(config.baseUri).path("long-running/{mode}/{echo}")
                .resolveTemplate("mode", (config.sync) ? "sync" : "async");

        final CountDownLatch latch = new CountDownLatch(config.requests);
        final Queue<String> errors = new ConcurrentLinkedQueue<String>();
        final AtomicInteger requestCounter = new AtomicInteger(0);

        final long tic = System.currentTimeMillis();
        for (int i = 0; i < config.requests; i++) {
            final int reqId = i;
            echoResource.resolveTemplate("echo", reqId).request().async().get(new InvocationCallback<String>() {
                private final AtomicInteger retries = new AtomicInteger(0);

                @Override
                public void completed(String response) {
                    final String requestId = Integer.toString(reqId);
                    if (requestId.equals(response)) {
                        System.out.print("*");
                        requestCounter.incrementAndGet();
                    } else {
                        System.out.print("!");
                        errors.offer(String.format("Echo response '%s' not equal to request '%s'", response, requestId));
                    }
                    latch.countDown();
                }

                @Override
                public void failed(Throwable error) {
                    if (error.getCause() instanceof IOException && retries.getAndIncrement() < 3) {
                        // resend
                        echoResource.resolveTemplate("echo", reqId).request().async().get(this);
                    } else {
                        System.out.print("!");
                        errors.offer(String.format("Request '%d' has failed: %s", reqId, error.toString()));
                        latch.countDown();
                    }
                }
            });
        }

        try {
            if (!latch.await(60, TimeUnit.SECONDS)) {
                errors.offer("Waiting for requests to complete has timed out.");
            }
        } catch (InterruptedException e) {
            errors.offer("Waiting for requests to complete has been interrupted.");
        }
        final long toc = System.currentTimeMillis();

        System.out.println(String.format("\n\nExecution finished in %d ms.\nSuccess rate: %6.2f %%",
                toc - tic,
                ((double) requestCounter.get() / config.requests) * 100));
        if (errors.size() > 0) {
            System.out.println("Following errors occurred during the request execution");
            for (String error : errors) {
                System.out.println("\t" + error);
            }
        }

        client.close();

        return errors.size() > 0 ? -1 : 0;
    }

    static class Config {

        /**
         * Default base URI of the async echo web application.
         */
        public static final String DEFAULT_BASE_URI = "http://localhost:8080/server-async-standalone-webapp/";

        final String baseUri;
        final boolean sync;
        final int requests;

        Config(String baseUri, boolean sync, int requests) {
            this.baseUri = baseUri;
            this.sync = sync;
            this.requests = requests;
        }

        public static Config parse(String[] args) {
            String baseUri = DEFAULT_BASE_URI;
            boolean sync = false;
            int requests = 10;

            for (String arg : args) {
                final String[] keyValuePair = arg.trim().split("=");

                if ("uri".equals(keyValuePair[0])) {
                    baseUri = keyValuePair[1];
                } else if ("mode".equals(keyValuePair[0])) {
                    sync = "sync".equals(keyValuePair[1]);
                } else if ("req".equals(keyValuePair[0])) {
                    requests = Integer.parseInt(keyValuePair[1]);
                } else {
                    System.out.println("WARNING: Unknown parameter: " + keyValuePair[0]);
                }
            }

            return new Config(baseUri, sync, requests);
        }

        @Override
        public String toString() {
            return "Config{"
                    + "baseUri='" + baseUri + '\''
                    + ", sync=" + sync
                    + ", requests=" + requests
                    + '}';
        }
    }
}
