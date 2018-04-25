/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.MessageUtils;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public abstract class ClipboardDataProvider implements MessageBodyWriter, MessageBodyReader {

    @Provider
    @Consumes("text/plain")
    @Produces("text/plain")
    public static class TextPlain extends ClipboardDataProvider {

        @Override
        public void writeTo(final Object t, final Class type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap httpHeaders, final OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(t.toString().getBytes(MessageUtils.getCharset(mediaType)));
        }

        @Override
        public Object readFrom(final Class type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType, final MultivaluedMap httpHeaders, final InputStream entityStream)
                throws IOException, WebApplicationException {
            return new ClipboardData(readStringFromStream(entityStream, MessageUtils.getCharset(mediaType)));
        }
    }

    @Provider
    @Consumes("application/json")
    @Produces("application/json")
    public static class ApplicationJson extends ClipboardDataProvider {

        private static final String JsonOpenning = "{\"content\":\"";
        private static final String JsonClosing = "\"}";

        @Override
        public void writeTo(final Object t, final Class type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap httpHeaders, final OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(String.format("%s%s%s", JsonOpenning, t.toString(), JsonClosing)
                    .getBytes(MessageUtils.getCharset(mediaType)));
        }

        @Override
        public Object readFrom(final Class type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType, final MultivaluedMap httpHeaders, final InputStream entityStream)
                throws IOException, WebApplicationException {
            final String jsonExpression = readStringFromStream(entityStream, MessageUtils.getCharset(mediaType));
            return new ClipboardData(jsonExpression.replace(JsonOpenning, "").replace(JsonClosing, ""));
        }
    }

    @Override
    public boolean isWriteable(final Class type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return isKnownType(type, genericType);
    }

    private boolean isKnownType(final Class<?> type, final Type genericType) {
        return type.isAssignableFrom(ClipboardData.class)
                || (Collection.class.isAssignableFrom(type)
                    && (((ParameterizedType) genericType).getActualTypeArguments()[0]).equals(String.class));
    }

    @Override
    public long getSize(final Object t, final Class type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isReadable(final Class type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType) {
        return isKnownType(type, genericType);
    }

    private static String readStringFromStream(final InputStream entityStream, Charset charset) throws IOException {
        final StringBuilder result = new StringBuilder();
        final byte[] buf = new byte[2048];
        int i;
        while ((i = entityStream.read(buf)) != -1) {
            result.append(new String(buf, 0, i, charset));
        }
        return result.toString();
    }
}
