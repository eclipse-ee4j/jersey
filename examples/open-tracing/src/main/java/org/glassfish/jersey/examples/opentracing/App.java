/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.opentracing;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.opentracing.OpenTracingFeature;
import org.glassfish.jersey.opentracing.OpenTracingUtils;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import com.uber.jaeger.Configuration;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

/**
 * Open tracing example application.
 * <p>
 * Exposes OpenTracing-enabled REST application (with Jaeger registered as the Tracer)
 * and invokes one request from (also OpenTracing-enabled) Jersey client.
 * <p>
 * To visualise the traces, start Jaeger locally:
 * <pre>
 * docker run -d -p 5775:5775/udp -p 16686:16686 jaegertracing/all-in-one:travis-1278
 * </pre>
 * and go to {@code localhost:16686}.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
public class App {

    private static final URI BASE_URI = URI.create("http://localhost:8080/opentracing");

    public static void main(String[] args) {
        try {
            System.out.println("\"Hello World\" Jersey OpenTracing Example App");
            prepare();

            final ResourceConfig resourceConfig = new ResourceConfig(TracedResource.class,
                    OpenTracingFeature.class,
                    ReqFilterA.class, ReqFilterB.class,
                    RespFilterA.class, RespFilterB.class);

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
            server.start();

            System.out.println(String.format("Application started.\nTry out %s/application.wadl\n"
                    + "Stop the application using CTRL+C", BASE_URI));

            // do the first "example" request with tracing-enabled client to show something in Jaegger UI,
            // include some weird headers and accepted types, that will be visible in the span's tags
            Client client = ClientBuilder.newBuilder().register(OpenTracingFeature.class).build();
            client.target(BASE_URI).path("resource/managedClient").request()
                    .accept("text/plain", "application/json", "*/*")
                    .header("foo", "bar")
                    .header("foo", "baz")
                    .header("Hello", "World").get();

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Configures Jaeger tracer as the {@link GlobalTracer}.
     */
    private static void prepare() {
        GlobalTracer.register(
                new com.uber.jaeger.Configuration(
                        "OpenTracingTemporaryTest",
                        new Configuration.SamplerConfiguration("const", 1),
                        new Configuration.ReporterConfiguration(
                                true,
                                "localhost",
                                5775,
                                1000,
                                10000)
                ).getTracer()
        );
    }

    /**
     * No-op request filter, just to test, that it will be listed in the root level span's logs.
     * For demonstrating purposes, it resolves the "root" request span and logs into it. If it fails to resolve the span, it
     * creates a new ad-hoc span.
     */
    static class ReqFilterA implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            Span span = OpenTracingUtils
                    .getRequestSpan(requestContext)
                    .orElse(GlobalTracer.get().buildSpan("ad-hoc-span-reqA").startManual());
            span.log("ReqFilterA.filter() invoked");
        }
    }

    /**
     * No-op request filter, just to test, that it will be listed in the root level span's logs.
     * For demonstrating purposes, it resolves the "root" request span and logs into it. If it fails to resolve the span, it
     * creates a new ad-hoc span.
     */
    static class ReqFilterB implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            Span span = OpenTracingUtils
                    .getRequestSpan(requestContext)
                    .orElse(GlobalTracer.get().buildSpan("ad-hoc-span-reqB").startManual());
            span.log("ReqFilterB.filter() invoked");
        }
    }

    /**
     * No-op response filter, just to test, that it will be listed in the root level span's logs.
     */
    static class RespFilterA implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            Span span = OpenTracingUtils
                    .getRequestSpan(requestContext)
                    .orElse(GlobalTracer.get().buildSpan("ad-hoc-span-respA").startManual());
            span.log("RespFilterA.filter() invoked");
        }
    }

    /**
     * No-op response filter, just to test, that it will be listed in the root level span's logs.
     */
    static class RespFilterB implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            Span span = OpenTracingUtils
                    .getRequestSpan(requestContext)
                    .orElse(GlobalTracer.get().buildSpan("ad-hoc-span-respB").startManual());
            span.log("RespFilterB.filter() invoked");
        }
    }
}

