/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import jakarta.ws.rs.SeBootstrap;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        bootstrapConfigurationBuilder.property(SeBootstrap.Configuration.PORT, 8080);

        SeBootstrap.start(new MyApplication(), bootstrapConfigurationBuilder.build())
            .whenComplete((instance1, throwable) -> {
                try {
                    System.out.println("Press enter to exit");
                    System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept((i) -> i.stop())

            .toCompletableFuture().get();

        System.out.println("Exiting...");
    }
}
