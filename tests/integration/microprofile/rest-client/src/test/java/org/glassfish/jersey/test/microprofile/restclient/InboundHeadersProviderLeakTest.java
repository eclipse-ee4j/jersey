/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.glassfish.jersey.microprofile.restclient.InboundHeadersProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for issue #6101: application {@link InboundHeadersProvider} headers must not be
 * copied into the outbound request unless an explicit {@link ClientHeadersFactory} is used.
 */
public class InboundHeadersProviderLeakTest {

    private static final String INBOUND_ONLY = "X-Inbound-Only";
    private static final String BUILDER_HEADER = "X-Builder-Header";
    private static final String RESULT = "X-Test-Leaked";
    private static final String BUILDER_PRESENT = "X-Builder-Present";
    private static final String FACTORY_SAW_INBOUND = "X-Factory-Saw-Inbound";

    @Test
    public void inboundHeadersMustNotBeCopiedAutomatically() throws Exception {
        Set<Boolean> observed = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            TestClient client = RestClientBuilder.newBuilder()
                    .baseUri(URI.create("http://example.invalid"))
                    .register(new TestInboundHeadersProvider())
                    .register(new InspectingFilter())
                    .build(TestClient.class);

            try {
                try (Response response = client.invoke()) {
                    observed.add(Boolean.parseBoolean(response.getHeaderString(RESULT)));
                }
            } finally {
                ((AutoCloseable) client).close();
            }
        }

        assertEquals(Set.of(false), observed, () -> "observed=" + observed);
    }

    @Test
    public void restClientBuilderHeaderStillAppliedWithoutClientHeadersFactory() throws Exception {
        TestClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://example.invalid"))
                .header(BUILDER_HEADER, "from-builder")
                .register(new TestInboundHeadersProvider())
                .register(new InspectingFilter())
                .build(TestClient.class);

        try (Response response = client.invoke()) {
            assertFalse(Boolean.parseBoolean(response.getHeaderString(RESULT)));
            assertTrue(Boolean.parseBoolean(response.getHeaderString(BUILDER_PRESENT)));
        } finally {
            ((AutoCloseable) client).close();
        }
    }

    @Test
    public void inboundHeadersAvailableToExplicitClientHeadersFactory() throws Exception {
        TestClientWithFactory client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://example.invalid"))
                .register(new TestInboundHeadersProvider())
                .register(new InspectingFilter())
                .build(TestClientWithFactory.class);

        try (Response response = client.invoke()) {
            // Explicit factory may propagate inbound headers; InspectingFilter reports leak as true.
            assertTrue(Boolean.parseBoolean(response.getHeaderString(RESULT)));
            assertTrue(Boolean.parseBoolean(response.getHeaderString(FACTORY_SAW_INBOUND)));
        } finally {
            ((AutoCloseable) client).close();
        }
    }

    @Path("/")
    public interface TestClient {
        @GET
        Response invoke();
    }

    @Path("/")
    @RegisterClientHeaders(PropagatingClientHeadersFactory.class)
    public interface TestClientWithFactory {
        @GET
        Response invoke();
    }

    public static final class TestInboundHeadersProvider implements InboundHeadersProvider {
        @Override
        public Map<String, List<String>> inboundHeaders() {
            return Map.of(INBOUND_ONLY, List.of("must-not-be-sent"));
        }

        @Override
        public int hashCode() {
            // Matches issue reproducer: equal hash codes stress HashSet iteration order.
            return 0;
        }
    }

    public static final class PropagatingClientHeadersFactory implements ClientHeadersFactory {
        @Override
        public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                     MultivaluedMap<String, String> clientOutgoingHeaders) {
            MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
            result.putAll(clientOutgoingHeaders);
            result.putAll(incomingHeaders);
            result.putSingle(FACTORY_SAW_INBOUND, String.valueOf(incomingHeaders.containsKey(INBOUND_ONLY)));
            return result;
        }
    }

    public static final class InspectingFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext context) {
            boolean leaked = context.getHeaders().containsKey(INBOUND_ONLY);
            boolean builderPresent = context.getHeaders().containsKey(BUILDER_HEADER);
            Object factorySaw = context.getHeaders().getFirst(FACTORY_SAW_INBOUND);
            context.abortWith(Response.ok()
                    .header(RESULT, leaked)
                    .header(BUILDER_PRESENT, builderPresent)
                    .header(FACTORY_SAW_INBOUND, factorySaw != null ? factorySaw : "false")
                    .build());
        }
    }
}
