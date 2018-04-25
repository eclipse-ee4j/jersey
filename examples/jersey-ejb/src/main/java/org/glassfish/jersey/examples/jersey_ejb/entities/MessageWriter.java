/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.entities;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.ejb.Singleton;
import org.glassfish.jersey.message.MessageUtils;

/**
 * A simple message body writer to serialize a single message bean.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Singleton
@Provider
public class MessageWriter implements MessageBodyWriter<Message> {

    @Override
    public boolean isWriteable(final Class<?> clazz, final Type type, final Annotation[] annotation, final MediaType mediaType) {
        return clazz == Message.class;
    }

    @Override
    public long getSize(final Message message, final Class<?> clazz, final Type type, final Annotation[] annotation,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Message message, final Class<?> clazz, final Type type, final Annotation[] annotation,
                        final MediaType mediaType, final MultivaluedMap<String, Object> arg5, final OutputStream ostream)
            throws IOException, WebApplicationException {
        ostream.write(message.toString().getBytes(MessageUtils.getCharset(mediaType)));
    }
}
