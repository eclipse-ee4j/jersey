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

package org.glassfish.jersey.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbortTest {
    private static final String TEXT_CSV = "text/csv";
    private static final String TEXT_HEADER = "text/header";
    private static final String EXPECTED_CSV = "hello;goodbye\nsalutations;farewell";
    private static final List<List<String>> CSV_LIST = Arrays.asList(
            Arrays.asList("hello", "goodbye"),
            Arrays.asList("salutations", "farewell")
    );

    @Test
    void testAbortWithGenericEntity() {
        Client client = ClientBuilder.newBuilder()
                .register(AbortRequestFilter.class)
                .register(CsvWriter.class)
                .build();
        String csvString = client.target("http://localhost:8080")
                .request(TEXT_CSV)
                .get(String.class);
        assertEquals(EXPECTED_CSV, csvString);
        client.close();
    }

    public static class AbortRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) {
            requestContext.abortWith(Response.ok(new GenericEntity<List<List<String>>>(CSV_LIST) {
            }).type(TEXT_CSV).build());
        }
    }

    @Produces(TEXT_CSV)
    public static class CsvWriter implements MessageBodyWriter<List<List<String>>> {

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType
                    && ((ParameterizedType) genericType).getActualTypeArguments()[0] instanceof ParameterizedType
                    && String.class.equals(((ParameterizedType) ((ParameterizedType) genericType).getActualTypeArguments()[0])
                        .getActualTypeArguments()[0]);
        }

        @Override
        public void writeTo(List<List<String>> csvList, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            List<String> rows = new ArrayList<>();
            for (List<String> row : csvList) {
                rows.add(String.join(";", row));
            }
            String csv = String.join("\n", rows);

            entityStream.write(csv.getBytes(StandardCharsets.UTF_8));
            entityStream.flush();
        }
    }

    @Test
    void testAbortWithMBWWritingHeaders() {
        final String entity = "HI";
        final String header = "CUSTOM_HEADER";
        try (Response response = ClientBuilder.newClient().register(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.abortWith(Response.ok(entity, TEXT_HEADER).build());
            }
        }).register(new MessageBodyWriter<String>() {

            @Override
            public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                return mediaType.toString().equals(TEXT_HEADER);
            }

            @Override
            public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                    WebApplicationException {
                httpHeaders.add(header, entity);
                entityStream.write(s.getBytes());
            }
        }, Priorities.USER - 1).target("http://localhost:8080").request().get()) {
            Assertions.assertEquals(entity, response.readEntity(String.class));
            Assertions.assertEquals(entity, response.getHeaderString(header));
        }
    }

}
