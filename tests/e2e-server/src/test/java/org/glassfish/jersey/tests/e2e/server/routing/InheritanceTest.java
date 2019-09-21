/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.routing;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class InheritanceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class).register(WriterA.class).register(WriterB.class).register(WriterString.class);
    }

    public static class A {

        String value;

        public A() {
            this.value = "a";
        }

        public A(final String value) {
            this.value = value;
        }
    }

    public static class B extends A {

        public B() {
            super("b");
        }

        public B(final String value) {
            super("b" + value);
        }
    }

    @Path("/")
    public static class Resource {

        @GET
        public A getA() {
            return new B("a");
        }
    }

    public static class WriterA implements MessageBodyWriter<A> {

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return A.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(final A a, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final A a, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(("a" + a.value).getBytes());
        }
    }

    @Produces({"text/plain"})
    public static class WriterString implements MessageBodyWriter<String> {

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final String s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final String s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(s.getBytes());
        }
    }

    @Produces("b/b")
    public static class WriterB implements MessageBodyWriter<B> {

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return B.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(final B b, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final B b,
                            final Class<?> type,
                            final Type genericType,
                            final Annotation[] annotations,
                            final MediaType mediaType,
                            final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(("b" + b.value).getBytes());
        }
    }

    @Test
    public void testWriterEntityInheritance() throws Exception {
        assertThat(target().request("a/b").get(String.class), equalTo("aba"));
    }
}
