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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.message.MessageUtils;

/**
 * @author Martin Matula
 */
@Consumes(MediaType.TEXT_PLAIN)
public class PropertiesReader implements MessageBodyReader<Set<String>> {

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return Set.class.isAssignableFrom(type);
    }

    @Override
    public Set<String> readFrom(final Class<Set<String>> type,
                                final Type genericType,
                                final Annotation[] annotations,
                                final MediaType mediaType,
                                final MultivaluedMap<String, String> httpHeaders,
                                final InputStream entityStream) throws IOException, WebApplicationException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(entityStream, MessageUtils.getCharset(mediaType)));
        final Set<String> result = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
            result.add(line);
        }
        return result;
    }
}
