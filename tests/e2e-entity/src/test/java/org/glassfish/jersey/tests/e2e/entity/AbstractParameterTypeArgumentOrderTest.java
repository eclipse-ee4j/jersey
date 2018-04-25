/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.test.JerseyTest;

/**
 * Parent for set of ParameterTypeArgumentOrder tests.
 *
 * Contains all the providers and resources for the tests. The resource config creation and test methods were separated into
 * subclasses.
 *
 * @author Paul Sandoz
 */
public abstract class AbstractParameterTypeArgumentOrderTest extends JerseyTest {

    @Provider
    public static class ObjectWriter implements MessageBodyWriter {

        @Override
        public boolean isWriteable(final Class type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final Object o, final Class type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final Object o, final Class type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap httpHeaders, final OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(o.toString().getBytes());
        }
    }

    public static class GenericClassWriter<T> implements MessageBodyWriter<T> {

        private final Class c;

        GenericClassWriter(final Class c) {
            this.c = c;
        }

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return c.isAssignableFrom(type);
        }

        @Override
        public long getSize(final T t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final T t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write((c.getSimpleName() + type.getSimpleName()).getBytes());
        }
    }

    public static class A {}

    @Provider
    public static class AWriter extends GenericClassWriter<A> {

        public AWriter() {
            super(A.class);
        }
    }

    public static class B extends A {}

    @Provider
    public static class BWriter extends GenericClassWriter<B> {

        public BWriter() {
            super(B.class);
        }
    }

    public static class C extends B {}

    @Provider
    public static class CWriter extends GenericClassWriter<C> {

        public CWriter() {
            super(C.class);
        }
    }

    @Path("/")
    public static class ClassResource {

        @GET
        @Path("a")
        public A getA() {
            return new A();
        }

        @GET
        @Path("b")
        public B getB() {
            return new B();
        }

        @GET
        @Path("c")
        public C getC() {
            return new C();
        }
    }

    public static class GenericClassReaderWriter<T> implements MessageBodyWriter<T>, MessageBodyReader<T> {

        private final Class c;

        GenericClassReaderWriter(final Class c) {
            this.c = c;
        }

        @Override
        public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                  final MediaType mediaType) {
            return c.isAssignableFrom(type);
        }

        @Override
        public T readFrom(final Class<T> type, final Type genericType, final Annotation[] annotations,
                          final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                          final InputStream entityStream) throws IOException, WebApplicationException {
            try {
                return (T) c.newInstance();
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return c.isAssignableFrom(type);
        }

        @Override
        public long getSize(final T t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final T t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write((c.getSimpleName() + type.getSimpleName()).getBytes());
        }
    }

    @Provider
    public static class AReaderWriter<T> extends GenericClassReaderWriter<T> {

        public AReaderWriter() {
            super(A.class);
        }
    }

    @Provider
    public static class BReaderWriter extends GenericClassReaderWriter<B> {

        public BReaderWriter() {
            super(B.class);
        }
    }

    @Provider
    public static class CReaderWriter extends GenericClassReaderWriter<C> {

        public CReaderWriter() {
            super(C.class);
        }
    }

}
