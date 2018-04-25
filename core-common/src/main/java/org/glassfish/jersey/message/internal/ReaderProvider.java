/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Singleton;

import org.glassfish.jersey.message.MessageUtils;

/**
 *
 * @author Paul Sandoz
 */
@Produces({"text/plain", "*/*"})
@Consumes({"text/plain", "*/*"})
@Singleton
public final class ReaderProvider extends AbstractMessageReaderWriterProvider<Reader> {

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType) {
        return Reader.class == type;
    }

    @Override
    public Reader readFrom(
            final Class<Reader> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream inputStream) throws IOException {

        final EntityInputStream entityStream = EntityInputStream.create(inputStream);
        if (entityStream.isEmpty()) {
            return new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(new byte[0]), MessageUtils.getCharset(mediaType)));
        }

        return new BufferedReader(new InputStreamReader(entityStream, getCharset(mediaType)));
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return Reader.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(
            final Reader t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException {
        try {
            final OutputStreamWriter out = new OutputStreamWriter(entityStream,
                    getCharset(mediaType));
            writeTo(t, out);
            out.flush();
        } finally {
            t.close();
        }

    }
}
