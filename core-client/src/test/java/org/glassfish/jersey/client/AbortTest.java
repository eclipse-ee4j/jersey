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

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.EntityPart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbortTest {
    private static final String TEXT_CSV = "text/csv";
    private static final String TEXT_HEADER = "text/header";
    private static final String EXPECTED_CSV = "hello;goodbye\nsalutations;farewell";
    private static final List<List<String>> CSV_LIST = Arrays.asList(
            Arrays.asList("hello", "goodbye"),
            Arrays.asList("salutations", "farewell")
    );
    private final String entity = "HI";
    private final String header = "CUSTOM_HEADER";


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

    @Test
    void testInterceptorHeaderAdd() {
        final String header2 = "CUSTOM_HEADER_2";

        try (Response response = ClientBuilder.newClient().register(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.abortWith(Response.ok().entity(entity).build());
            }
        }).register(new ReaderInterceptor() {
                    @Override
                    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
                        MultivaluedMap<String, String> headers = context.getHeaders();
                        headers.put(header, Collections.singletonList(entity));
                        headers.add(header2, entity);
                        return context.proceed();
                    }
                })
                .target("http://localhost:8080").request().get()) {
            Assertions.assertEquals(entity, response.readEntity(String.class));
            Assertions.assertEquals(entity, response.getHeaderString(header));
            Assertions.assertEquals(entity, response.getHeaderString(header2));
        }
    }

    @Test
    void testInterceptorHeaderIterate() {
        final AtomicReference<String> originalHeader = new AtomicReference<>();

        try (Response response = ClientBuilder.newClient().register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.abortWith(Response.ok().header(header, header).entity(entity).build());
                    }
                }).register(new ReaderInterceptor() {
                    @Override
                    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
                        MultivaluedMap<String, String> headers = context.getHeaders();
                        Iterator<Map.Entry<String, List<String>>> it = headers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, List<String>> next = it.next();
                            if (header.equals(next.getKey())) {
                                originalHeader.set(next.setValue(Collections.singletonList(entity)).get(0));
                            }
                        }
                        return context.proceed();
                    }
                })
                .target("http://localhost:8080").request().get()) {
            Assertions.assertEquals(entity, response.readEntity(String.class));
            Assertions.assertEquals(entity, response.getHeaderString(header));
            Assertions.assertEquals(header, originalHeader.get());
        }
    }

    @Test
    void testNullHeader() {
        final AtomicReference<String> originalHeader = new AtomicReference<>();
        RuntimeDelegate.setInstance(new StringHeaderRuntimeDelegate(RuntimeDelegate.getInstance()));
        try (Response response = ClientBuilder.newClient().register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.abortWith(Response.ok()
                                .header(header, new StringHeader())
                                .entity(entity).build());
                    }
                }).register(new ClientResponseFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
                            throws IOException {
                        originalHeader.set(responseContext.getHeaderString(header));
                    }
                })
                .target("http://localhost:8080").request().get()) {
            Assertions.assertEquals(entity, response.readEntity(String.class));
            Assertions.assertEquals("", originalHeader.get());
        }
    }

    @Test
    public void testResponseContextCaseInsensitiveKeys() {
        try (Response response = ClientBuilder.newClient()
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.abortWith(Response.ok()
                                .header("header1", "value")
                                .header("header1", "value1 , value2")
                                .header("header1", "Value3,white space ")
                                .header("header2", "Value4;;Value5")
                                .build());
                    }
                })
                .register(new ClientResponseFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext, ClientResponseContext context) {
                        Assertions.assertTrue(context.getHeaderString("header1").contains("value"));
                        Assertions.assertTrue(context.getHeaderString("HEADER1").contains("value2"));
                        //White space in value not trimmed
                        Assertions.assertFalse(context.getHeaderString("header1").contains("whitespace"));
                        //Multiple character separator
                        Assertions.assertTrue(context.getHeaderString("header2").contains("Value5"));

                        Assertions.assertTrue(context.getHeaders().containsKey("HEADer1"));
                        Assertions.assertFalse(context.getHeaders().get("HEADer1").isEmpty());
                        Assertions.assertFalse(context.getHeaders().remove("HeAdEr1").isEmpty());
                        Assertions.assertFalse(context.getHeaders().containsKey("header1"));
                    }
                })
                .target("http://localhost:8080")
                .request()
                .get()) {
            Assertions.assertEquals(200, response.getStatus());
        }
    }

    private static class StringHeader extends AtomicReference<String> {

    }

    private static class StringHeaderDelegate implements RuntimeDelegate.HeaderDelegate<StringHeader> {
        @Override
        public StringHeader fromString(String value) {
            StringHeader stringHeader = new StringHeader();
            stringHeader.set(value);
            return stringHeader;
        }

        @Override
        public String toString(StringHeader value) {
            //on purpose
            return null;
        }
    }

    private static class StringHeaderRuntimeDelegate extends RuntimeDelegate {
        private final RuntimeDelegate original;

        private StringHeaderRuntimeDelegate(RuntimeDelegate original) {
            this.original = original;
        }

        @Override
        public UriBuilder createUriBuilder() {
            return original.createUriBuilder();
        }

        @Override
        public Response.ResponseBuilder createResponseBuilder() {
            return original.createResponseBuilder();
        }

        @Override
        public Variant.VariantListBuilder createVariantListBuilder() {
            return original.createVariantListBuilder();
        }

        @Override
        public <T> T createEndpoint(Application application, Class<T> endpointType)
                throws IllegalArgumentException, UnsupportedOperationException {
            return original.createEndpoint(application, endpointType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
            if (StringHeader.class.equals(type)) {
                return (HeaderDelegate<T>) new StringHeaderDelegate();
            }
            return original.createHeaderDelegate(type);
        }

        @Override
        public Link.Builder createLinkBuilder() {
            return original.createLinkBuilder();
        }

        @Override
        public SeBootstrap.Configuration.Builder createConfigurationBuilder() {
            return original.createConfigurationBuilder();
        }

        @Override
        public CompletionStage<SeBootstrap.Instance> bootstrap(Application application, SeBootstrap.Configuration configuration) {
            return original.bootstrap(application, configuration);
        }

        @Override
        public CompletionStage<SeBootstrap.Instance> bootstrap(Class<? extends Application> clazz,
                                                               SeBootstrap.Configuration configuration) {
            return original.bootstrap(clazz, configuration);
        }

        @Override
        public EntityPart.Builder createEntityPartBuilder(String partName) throws IllegalArgumentException {
            return original.createEntityPartBuilder(partName);
        }
    }

}
