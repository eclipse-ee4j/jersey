/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.j6000;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.spi.WebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@ApplicationPath("/")
@ApplicationScoped
public class TestServer extends Application implements AutoCloseable {

    private CompletionStage<SeBootstrap.Instance> instance;

    public CompletionStage<SeBootstrap.Instance> startServer(int port) throws GeneralSecurityException, IOException {
        jakarta.ws.rs.SeBootstrap.Configuration.Builder builder = SeBootstrap.Configuration.builder()
                .port(port)
                .rootPath("/")
                .protocol("HTTP");

        this.instance = SeBootstrap.start(this, builder.build());
        this.instance.thenAccept(i -> {
            System.out.println("server started");
        });
        return this.instance;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PingApi.class);
    }

    @Override
    public void close() throws Exception {
    }

    @Test
    void testJerseyBeanGetBeanClass() throws GeneralSecurityException, IOException, ExecutionException, InterruptedException {
        final String sPort = System.getProperty("jersey.config.test.container.port");
        int port = sPort == null || sPort.isEmpty() ? 8080 : Integer.parseInt(sPort);
        TestServer server = new TestServer();
        SeBootstrap.Instance instance = server.startServer(port).toCompletableFuture().get();
        try {
            port = instance.unwrap(WebServer.class).port();
            try (Response response = ClientBuilder.newClient().target("http://localhost:" + port + "/ping").request().get()) {
                Assertions.assertEquals(200, response.getStatus());
            }
        } finally {
            instance.stop();
        }
    }
}
