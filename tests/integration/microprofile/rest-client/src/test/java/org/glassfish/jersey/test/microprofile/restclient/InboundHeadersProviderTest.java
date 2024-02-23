/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.microprofile.restclient;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.glassfish.jersey.microprofile.restclient.InboundHeadersProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests if {@link InboundHeadersProvider} is called when it is actually needed.
 */
public class InboundHeadersProviderTest extends JerseyTest {

    private static final String EXECUTED = "provide-executed";
    private static TestClient clientWithoutProvider;
    private static TestClient clientWithProvider;

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(HeaderProviderTestResource.class);
    }

    @BeforeAll
    public static void clientSetup() {
        clientWithoutProvider = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:9998"))
                .build(TestClient.class);

        clientWithProvider = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:9998"))
                .register(new TestInboundHeadersProvider())
                .build(TestClient.class);
    }

    @Test
    public void providerExecuted() {
        JsonObject expected = Json.createObjectBuilder()
                .add("executed", true)
                .build();
        JsonObject jsonObject = clientWithoutProvider.headersProviderExecuted();
        assertEquals(expected, jsonObject);
    }

    @Test
    public void providerNotExecuted() {
        JsonObject expected = Json.createObjectBuilder()
                .add("executed", false)
                .build();
        JsonObject jsonObject = clientWithoutProvider.headersProviderNotExecuted();
        assertEquals(expected, jsonObject);
    }

    @Path("/")
    @RegisterClientHeaders(CustomClientHeadersFactory.class)
    public interface TestClient {

        @GET
        JsonObject object();

        @GET
        @Path("executed")
        JsonObject headersProviderExecuted();

        @GET
        @Path("notExecuted")
        JsonObject headersProviderNotExecuted();

    }

    @Path("/")
    public static final class HeaderProviderTestResource {

        @Context
        private HttpHeaders headers;

        @Context
        private ExecutorService executorService;

        @GET
        public JsonObject object() {
            return Json.createObjectBuilder()
                    .add("executed", headers.getHeaderString(EXECUTED) != null)
                    .build();
        }

        @GET
        @Path("executed")
        public void headersProviderExecuted(@Suspended AsyncResponse response) {
            CompletableFuture.supplyAsync(clientWithProvider::object, executorService)
                    .thenAccept(response::resume)
                    .exceptionally(t -> {
                        response.resume(null);
                        return null;
                    });
        }

        @GET
        @Path("notExecuted")
        public JsonObject headersProviderNotExecuted() {
            return clientWithProvider.object();
        }
    }

    public static final class CustomClientHeadersFactory implements ClientHeadersFactory {

        public CustomClientHeadersFactory() {
        }

        @Override
        public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                     MultivaluedMap<String, String> clientOutgoingHeaders) {
            MultivaluedMap<String, String> resultHeaders = new MultivaluedHashMap<>();
            resultHeaders.putAll(clientOutgoingHeaders);
            resultHeaders.putAll(incomingHeaders);

            return resultHeaders;
        }
    }

    private static final class TestInboundHeadersProvider implements InboundHeadersProvider {
        @Override
        public Map<String, List<String>> inboundHeaders() {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put(EXECUTED, Collections.singletonList("executed"));
            return headers;
        }
    }

}
