/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark.headers;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class HeadersMBRW implements MessageBodyReader<String>, MessageBodyWriter<String> {

    private static final JacksonJsonProvider jackson = new JacksonJsonProvider();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (type == String.class && HeadersResource.MEDIA_PLAIN.equals(mediaType.toString()))
                || (type == String.class && HeadersResource.MEDIA_JSON.equals(mediaType.toString()));
    }

    @Override
    public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        switch (mediaType.toString()) {
            case HeadersResource.MEDIA_PLAIN:
                return ReaderWriter.readFromAsString(entityStream, MediaType.TEXT_PLAIN_TYPE);
            case HeadersResource.MEDIA_JSON:
                return jackson.readFrom((Class<Object>) (Class) type, genericType, annotations, mediaType,
                        httpHeaders, entityStream).toString();
        }
        return null;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        switch (mediaType.toString()) {
            case HeadersResource.MEDIA_PLAIN:
                ReaderWriter.writeToAsString(s, entityStream, MediaType.TEXT_PLAIN_TYPE);
                break;
            case HeadersResource.MEDIA_JSON:
                jackson.writeTo(s, type, genericType, annotations, MediaType.APPLICATION_JSON_TYPE, httpHeaders, entityStream);
                break;
        }
    }
}
