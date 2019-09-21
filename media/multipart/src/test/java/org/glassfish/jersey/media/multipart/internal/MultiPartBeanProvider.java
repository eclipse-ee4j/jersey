/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS <code>Provider</code> that knows how to serialize and deserialize
 * {@link MultiPartBean} instances.
 */
@Provider
@Consumes("x-application/x-format")
@Produces("x-application/x-format")
public class MultiPartBeanProvider implements MessageBodyReader<MultiPartBean>, MessageBodyWriter<MultiPartBean> {

    private static final MediaType CUSTOM_MEDIA_TYPE = new MediaType("x-application", "x-format");

    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {

        return type.isAssignableFrom(MultiPartBean.class) && mediaType.equals(CUSTOM_MEDIA_TYPE);
    }

    public MultiPartBean readFrom(final Class<MultiPartBean> type,
                                  final Type genericType,
                                  final Annotation[] annotations,
                                  final MediaType mediaType,
                                  final MultivaluedMap<String, String> headers,
                                  final InputStream stream) throws IOException, WebApplicationException {

        final InputStreamReader reader = new InputStreamReader(stream);
        final StringBuilder sb = new StringBuilder();

        while (true) {
            int ch = reader.read();
            if ((ch < 0) || ((char) ch == '\n')) {
                break;
            } else {
                sb.append((char) ch);
            }
        }

        String line = sb.toString();
        int equals = line.indexOf('=');
        if (equals < 0) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Input content '" + line + "' is not in a valid format"));
        }

        return new MultiPartBean(line.substring(0, equals), line.substring(equals + 1));
    }

    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {

        return type.isAssignableFrom(MultiPartBean.class) && mediaType.equals(CUSTOM_MEDIA_TYPE);
    }

    public long getSize(final MultiPartBean entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    public void writeTo(final MultiPartBean entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> headers,
                        final OutputStream stream) throws IOException, WebApplicationException {

        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(entity.getName());
        writer.write('=');
        writer.write(entity.getValue());
        writer.write('\n');
        writer.flush();
    }

}
