/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.tests.performance.benchmark.server.jnh.Book;
import org.glassfish.jersey.tests.performance.benchmark.server.jnh.BookShelfApplication;
import org.glassfish.jersey.tests.performance.benchmark.server.jnh.BookShelfResource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * jersey-jnh-connector {@link org.glassfish.jersey.jnh.connector.JavaNetHttpConnector} benchmark.
 *
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 16, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class JNHConnectorBenchmark {

    private static final URI BASE_URI = URI.create("http://localhost:8080/");

    private static final String REQUEST_TARGET = "HttpMethod";
    private volatile HttpServer server;

    private volatile Client client;
    private volatile Client defaultClient;

    private final Book postBook = new Book("The Bonfire of the Vanities", "Tom Wolfe", 11);
    private final Book postDefaultBook = new Book("More Die of Heartbreak", "Saul Bellow", 22);
    private final Book putBook = new Book("Silmarillion", "J. R. R. Tolkien", 110);
    private final Book putDefaultBook = new Book("The Lord of the Rings", "John Tolkien", 220);

    @Setup
    public void start() throws Exception {
        server =
                GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new BookShelfApplication(), false);
        final TCPNIOTransport transport = server.getListener("grizzly").getTransport();
        transport.setSelectorRunnersCount(4);
        transport.setWorkerThreadPoolConfig(ThreadPoolConfig.defaultConfig().setCorePoolSize(8).setMaxPoolSize(8));

        server.start();
        client = ClientBuilder.newClient(
                new ClientConfig()
                        .connectorProvider(new JavaNetHttpConnectorProvider())
                        .register(JacksonFeature.class)
        );
        defaultClient = ClientBuilder.newClient(
                new ClientConfig()
                        .register(JacksonFeature.class)
        );

    }

    @Setup(Level.Iteration)
    public void request() {
    }

    @TearDown
    public void shutdown() {
        server.shutdownNow();
    }

    @Benchmark
    public void measureGetStringResource() {
        client.target(BASE_URI).path(REQUEST_TARGET).request(MediaType.TEXT_PLAIN).get(String.class);
    }

    @Benchmark
    public void measureGetStringDefaultResource() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).request(MediaType.TEXT_PLAIN).get(String.class);
    }

    @Benchmark
    public void measureEntityGetResource() {
        client.target(BASE_URI).path(REQUEST_TARGET).path("books")
                .request(MediaType.APPLICATION_JSON_TYPE).get();
    }
    @Benchmark
    public void measureEntityGetXmlResource() throws IOException {
        final InputStream stream = client.target(BASE_URI).path(REQUEST_TARGET).path("books")
                .request(MediaType.APPLICATION_XML_TYPE).get(InputStream.class);
        try {
            System.out.print(new String(stream.readAllBytes()).charAt(0));
        } finally {
            stream.close();
        }
    }

    @Benchmark
    public void measureEntityGetOneXmlResource() throws IOException {
        final Book book = client.target(BASE_URI).path(REQUEST_TARGET).path("book")
                .request(MediaType.APPLICATION_XML_TYPE).get(Book.class);
        System.out.print(book.getId());
    }
    @Benchmark
    public void measureEntityGetDefaultResource() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("books")
                .request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    @Benchmark
    public void measureEntityGetXmlDefaultResource() throws IOException {
        final InputStream stream = defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("books")
                .request(MediaType.APPLICATION_XML_TYPE).get(InputStream.class);
        try {
            System.out.print(new String(stream.readAllBytes()).charAt(0));
        } finally {
            stream.close();
        }
    }
    @Benchmark
    public void measureEntityGetOneXmlDefaultResource() {
        final Book book = defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("book")
                .request(MediaType.APPLICATION_XML_TYPE).get(Book.class);
        System.out.print(book.getId());
    }

    @Benchmark
    public void measurePost() {
        client.target(BASE_URI).path(REQUEST_TARGET).path("postBook")
                .request().post(Entity.json(postBook));
    }

    @Benchmark
    public void measurePostDefault() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("postBook")
                .request().post(Entity.json(postDefaultBook));
    }

    @Benchmark
    public void measureBigPost() {
        client.target(BASE_URI).path(REQUEST_TARGET).path("postBooks")
                .request().post(Entity.entity(convertLongEntity(BookShelfResource.bookList), MediaType.TEXT_PLAIN_TYPE));
    }

    @Benchmark
    public void measureBigPostDefault() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("postBooks")
                .request().post(Entity.entity(convertLongEntity(BookShelfResource.bookList), MediaType.TEXT_PLAIN_TYPE));
    }

    private static final StreamingOutput convertLongEntity(List<Book> books) {
        return out -> {
            int offset = 0;
            while (offset < books.size()) {
                out.write(books.get(offset).toByteArray());
                out.write('\n');
                offset++;
            }
        };
    }


    @Benchmark
    public void measurePut() {
        client.target(BASE_URI).path(REQUEST_TARGET).path("putBook")
                .request().put(Entity.json(putBook));
    }

    @Benchmark
    public void measurePutDefault() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("putBook")
                .request().put(Entity.json(putDefaultBook));
    }

    @Benchmark
    public void measureDelete() {
        client.target(BASE_URI).path(REQUEST_TARGET).path("deleteBook")
                .request().delete();
    }

    @Benchmark
    public void measureDeleteDefault() {
        defaultClient.target(BASE_URI).path(REQUEST_TARGET).path("deleteBook")
                .request().delete();
    }


    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                // Register our benchmarks.
                .include(JNHConnectorBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}