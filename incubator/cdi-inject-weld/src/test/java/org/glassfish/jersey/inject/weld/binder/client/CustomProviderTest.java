/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.binder.client;

import org.glassfish.jersey.inject.weld.ClientTestParent;
import org.glassfish.jersey.inject.weld.TestParent;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class CustomProviderTest extends ClientTestParent {

    private static final String URI = "http://somewhere.nevermind:60000";

    public static class POJO {
        private final String value;

        public POJO(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class POJOProvider implements MessageBodyReader<POJO>, MessageBodyWriter<POJO> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == POJO.class;
        }

        @Override
        public POJO readFrom(Class<POJO> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return new POJO(ReaderWriter.readFromAsString(entityStream, mediaType));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == POJO.class;
        }

        @Override
        public void writeTo(POJO pojo, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(pojo.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void testCustomRequestFilter() {
        Response r = ClientBuilder.newClient()
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        InjectionManager im = ((InjectionManagerSupplier) requestContext).getInjectionManager();
                        assertMultiple(im, ClientRequestFilter.class, 1, this.getClass().getSimpleName());
                        requestContext.abortWith(Response.ok().build());
                    }})
                .target(URI).request()
                .get();
        r.close();
    }

    @Test
    void testCustomMessageBodyReader() {
        Response r = ClientBuilder.newClient()
                .register(new POJOProvider())
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        InjectionManager im = ((InjectionManagerSupplier) requestContext).getInjectionManager();
                        assertMultiple(im, MessageBodyReader.class, 1, POJOProvider.class.getSimpleName());
                        requestContext.abortWith(Response.ok().build());
                    }})
                .target(URI).request()
                .get();
        r.close();
    }

    @Test
    void testCustomMessageBodyReaderClass() {
        Response r = ClientBuilder.newClient()
                .register(POJOProvider.class)
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        InjectionManager im = ((InjectionManagerSupplier) requestContext).getInjectionManager();
                        assertMultiple(im, MessageBodyReader.class, 1, POJOProvider.class.getSimpleName());
                        requestContext.abortWith(Response.ok().build());
                    }})
                .target(URI).request()
                .get();
        r.close();
        //post(Entity.entity(new POJO("hello"),MediaType.TEXT_PLAIN_TYPE));
    }

    static class InjectingClientRequestFilter implements ClientRequestFilter {
        @Inject
        Configuration configuration;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            Assertions.assertNotNull(configuration);
            Assertions.assertTrue(configuration.getClass().getName().contains("ClientConfig$State"));
            requestContext.abortWith(Response.ok().build());
        }
    }

    @Test
    void testCustomFilterProviderIsInjectable() {
        try (Response r = ClientBuilder.newClient().register(InjectingClientRequestFilter.class)
                .target(URI).request().get()) {
            Assertions.assertEquals(200, r.getStatus());
        }
    }
}
