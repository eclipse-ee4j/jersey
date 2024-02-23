/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Markus Karg. All rights reserved.
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
package org.glassfish.jersey.tests.e2e.server.sebootstrap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Compliance Test for Java SE Bootstrap API of Jakarta REST API
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1
 */
public class SeBootstrapTest {
    /**
     * Verifies that an instance will boot using default configuration.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldBootInstanceUsingDefaults() throws InterruptedException, ExecutionException {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is("HTTP"));
        assertThat(actualConfiguration.host(), is("localhost"));
        assertThat(actualConfiguration.port(), is(greaterThan(0)));
        assertThat(actualConfiguration.rootPath(), is("/"));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will boot using explicit configuration given by
     * properties.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     * @throws IOException          if no IP port was free
     */
    @Test
    public final void shouldBootInstanceUsingProperties() throws InterruptedException, ExecutionException, IOException {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder
                .property(SeBootstrap.Configuration.PROTOCOL, "HTTP")
                .property(SeBootstrap.Configuration.HOST, "localhost")
                .property(SeBootstrap.Configuration.PORT, someFreeIpPort())
                .property(SeBootstrap.Configuration.ROOT_PATH, "/root/path").build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(requestedConfiguration.port()));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will boot using explicit configuration given by
     * convenience methods.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     * @throws IOException          if no IP port was free
     */
    @Test
    public final void shouldBootInstanceUsingConvenienceMethods() throws InterruptedException, ExecutionException, IOException {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.protocol("HTTP").host("localhost")
                .port(someFreeIpPort()).rootPath("/root/path").build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(requestedConfiguration.port()));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will boot using external configuration.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     * @throws IOException          if no IP port was free
     */
    @Test
    public final void shouldBootInstanceUsingExternalConfiguration() throws Exception {
        // given
        final int someFreeIpPort = someFreeIpPort();
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.from((property, type) -> {
            switch (property) {
                case SeBootstrap.Configuration.PROTOCOL:
                    return Optional.of("HTTP");
                case SeBootstrap.Configuration.HOST:
                    return Optional.of("localhost");
                case SeBootstrap.Configuration.PORT:
                    return Optional.of(someFreeIpPort);
                case SeBootstrap.Configuration.ROOT_PATH:
                    return Optional.of("/root/path");
                default:
                    return Optional.empty();
            }
        }).build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(requestedConfiguration.port()));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will ignore unknown configuration parameters.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     * @throws IOException          if no IP port was free
     */
    @Test
    public final void shouldBootInstanceDespiteUnknownConfigurationParameters() throws Exception {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.protocol("HTTP").host("localhost")
                .port(someFreeIpPort()).rootPath("/root/path").from((property, type) -> {
            switch (property) {
                case "jakarta.ws.rs.tck.sebootstrap.SeBootstrapIT$Unknown_1":
                    return Optional.of("Silently ignored value A");
                default:
                    return Optional.empty();
            }
        }).property("jakarta.ws.rs.tck.sebootstrap.SeBootstrapIT$Unknown_2", "Silently ignored value B")
                .from(new Object()).build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(requestedConfiguration.port()));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will boot using a self-detected free IP port.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldBootInstanceUsingSelfDetectedFreeIpPort() throws InterruptedException, ExecutionException {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.protocol("HTTP").host("localhost")
                .port(SeBootstrap.Configuration.FREE_PORT).rootPath("/root/path").build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(greaterThan(0)));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    /**
     * Verifies that an instance will boot using the implementation's default IP
     * port.
     *
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldBootInstanceUsingImplementationsDefaultIpPort() throws InterruptedException, ExecutionException {
        // given
        final int expectedResponse = mockInt();
        final Application application = new StaticApplication(expectedResponse);
        final SeBootstrap.Configuration.Builder bootstrapConfigurationBuilder = SeBootstrap.Configuration.builder();
        final SeBootstrap.Configuration requestedConfiguration = bootstrapConfigurationBuilder.protocol("HTTP").host("localhost")
                .port(SeBootstrap.Configuration.DEFAULT_PORT).rootPath("/root/path").build();

        // when
        final CompletionStage<SeBootstrap.Instance> completionStage = SeBootstrap.start(application, requestedConfiguration);
        final SeBootstrap.Instance instance = completionStage.toCompletableFuture().get();
        final SeBootstrap.Configuration actualConfiguration = instance.configuration();
        final int actualResponse = client.target(UriBuilder.newInstance().scheme(actualConfiguration.protocol())
                .host(actualConfiguration.host()).port(actualConfiguration.port()).path(actualConfiguration.rootPath())
                .path("application/resource")).request().get(int.class);

        // then
        assertThat(actualResponse, is(expectedResponse));
        assertThat(actualConfiguration.protocol(), is(requestedConfiguration.protocol()));
        assertThat(actualConfiguration.host(), is(requestedConfiguration.host()));
        assertThat(actualConfiguration.port(), is(greaterThan(0)));
        assertThat(actualConfiguration.rootPath(), is(requestedConfiguration.rootPath()));
        instance.stop().toCompletableFuture().get();
    }

    private static Client client;

    @BeforeAll
    public static void createClient() {
        SeBootstrapTest.client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void disposeClient() {
        SeBootstrapTest.client.close();
    }

    @ApplicationPath("application")
    public static final class StaticApplication extends Application {

        private final StaticResource staticResource;

        private StaticApplication(final long staticResponse) {
            this.staticResource = new StaticResource(staticResponse);
        }

        @Override
        public final Set<Object> getSingletons() {
            return Collections.<Object>singleton(staticResource);
        }

        @Path("resource")
        public static final class StaticResource {

            private final long staticResponse;

            private StaticResource(final long staticResponse) {
                this.staticResponse = staticResponse;
            }

            @GET
            public final long staticResponse() {
                return this.staticResponse;
            }
        }
    }

    private static final int someFreeIpPort() throws IOException {
        int port = 0;
        int cnt = 0;
        while (port < 1024 && cnt++ < 1025) {
            try (final ServerSocket serverSocket = new ServerSocket(0)) {
                port = serverSocket.getLocalPort();
            }
        }
        return port;
    }

    private static final int mockInt() {
        return (int) Math.round(Integer.MAX_VALUE * Math.random());
    }
}
