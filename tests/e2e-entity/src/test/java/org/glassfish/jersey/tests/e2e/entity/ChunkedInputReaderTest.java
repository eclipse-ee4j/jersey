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


package org.glassfish.jersey.tests.e2e.entity;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkedInputReaderTest extends JerseyTest {

    @Path("/")
    public static class ChunkedInputReaderTestResource {
        @GET
        public String get() {
            return "To_be_replaced_by_client_reader";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ChunkedInputReaderTestResource.class);
    }

    @Test
    public void testChunkedInputStreamIsClosed() {
        AtomicBoolean closed = new AtomicBoolean(false);
        InputStream inputStream = new ByteArrayInputStream("TEST".getBytes()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        final GenericType<ChunkedInput<String>> chunkedInputGenericType = new GenericType(new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return ChunkedInput.class;
            }

            @Override
            public Type getOwnerType() {
                return ChunkedInput.class;
            }
        });


        ChunkedInput<String> response = target().register(new ReaderInterceptor() {
                    @Override
                    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
                        context.setInputStream(inputStream);
                        return context.proceed();
                    };
                }).request().get(chunkedInputGenericType);
        MatcherAssert.assertThat(response.read(), Matchers.is("TEST"));
        response.close();
        MatcherAssert.assertThat(closed.get(), Matchers.is(true));
    }
}
