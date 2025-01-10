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

package org.glassfish.jersey.test.microprofile.restclient;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpHeaderTest {
    @Path("/")
    public interface HeaderResource {
        @GET
        public String get();
    }

    @Test
    public void restclientBuilderWithHeadersTest() {
        String headerName = "BUILDER_HEADER";
        String headerValue = "BUILDER_VALUE";
        HeaderResource resource = RestClientBuilder.newBuilder()
                .baseUri("http://localhost:8080")
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        if (requestContext.getHeaders().containsKey(headerName)) {
                            requestContext.abortWith(Response.ok(requestContext.getHeaders().getFirst(headerName)).build());
                        } else {
                            requestContext.abortWith(Response.ok("no_header").build());
                        }
                    }
                })
                .header(headerName, headerValue)
                .build(HeaderResource.class);
        Assertions.assertEquals(headerValue, resource.get());
    }

    @ClientHeaderParam(name = "InterfaceAndBuilderHeader", value = "interface")
    @Path("/")
    public interface ClientBuilderHeaderClient {

        @GET
        JsonObject getAllHeaders(@HeaderParam("HeaderParam") String param);
    }

    public static class ReturnWithAllDuplicateClientHeadersFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            JsonObjectBuilder allClientHeaders = Json.createObjectBuilder();
            MultivaluedMap<String, Object> clientHeaders = clientRequestContext.getHeaders();
            for (String headerName : clientHeaders.keySet()) {
                List<Object> header = clientHeaders.get(headerName);
                final JsonArrayBuilder headerValues = Json.createArrayBuilder();
                header.forEach(h -> headerValues.add(h.toString()));
                allClientHeaders.add(headerName, headerValues);
            }
            clientRequestContext.abortWith(Response.ok(allClientHeaders.build()).build());
        }

    }

    @Test
    public void testHeaderBuilderInterface() {

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri("http://localhost:8080/");
        builder.register(ReturnWithAllDuplicateClientHeadersFilter.class);
        builder.header("InterfaceAndBuilderHeader", "builder");
        ClientBuilderHeaderClient client = builder.build(ClientBuilderHeaderClient.class);

        checkHeaders(client.getAllHeaders("headerparam"), "interface");
    }

    private static void checkHeaders(final JsonObject headers, final String clientHeaderParamName) {
        final List<String> clientRequestHeaders = headerValues(headers, "InterfaceAndBuilderHeader");

        assertTrue(clientRequestHeaders.contains("builder"),
                "Header InterfaceAndBuilderHeader did not container \"builder\": " + clientRequestHeaders);
        assertTrue(clientRequestHeaders.contains(clientHeaderParamName),
                "Header InterfaceAndBuilderHeader did not container \"" + clientHeaderParamName + "\": "
                        + clientRequestHeaders);

        final List<String> headerParamHeaders = headerValues(headers, "HeaderParam");
        assertTrue(headerParamHeaders.contains("headerparam"),
                "Header HeaderParam did not container \"headerparam\": " + headerParamHeaders);
    }

    private static List<String> headerValues(final JsonObject headers, final String headerName) {
        final JsonArray headerValues = headers.getJsonArray(headerName);
        Assertions.assertNotNull(headerValues,
                String.format("Expected header '%s' to be present in %s", headerName, headers));
        return headerValues.stream().map(
                        v -> (v.getValueType() == JsonValue.ValueType.STRING ? ((JsonString) v).getString() : v.toString()))
                .collect(Collectors.toList());
    }

}
