/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sysprops;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Set;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.message.MessageUtils;

/**
 * @author Martin Matula
 */
@Produces(MediaType.TEXT_PLAIN)
public class PropertiesWriter implements MessageBodyWriter<Set<String>> {
    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return Set.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final Set<String> s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Set<String> s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        final PrintStream ps = new PrintStream(entityStream, true, MessageUtils.getCharset(mediaType).name());

        for (final String item : s) {
            ps.println(item);
        }
    }
}
