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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.ejb.Stateless;

import org.glassfish.jersey.message.MessageUtils;

/**
 * A simple HTML message body writer to serialize list of message beans.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Stateless
@Provider
public class MessageListWriter implements MessageBodyWriter<List<Message>> {

    @Context
    private javax.inject.Provider<UriInfo> ui;

    @Override
    public boolean isWriteable(final Class<?> clazz, final Type type, final Annotation[] annotation, final MediaType mediaType) {
        return verifyGenericType(type);
    }

    private boolean verifyGenericType(final Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }

        final ParameterizedType pt = (ParameterizedType) genericType;

        if (pt.getActualTypeArguments().length > 1) {
            return false;
        }

        if (!(pt.getActualTypeArguments()[0] instanceof Class)) {
            return false;
        }

        final Class listClass = (Class) pt.getActualTypeArguments()[0];
        return listClass == Message.class;
    }

    @Override
    public long getSize(final List<Message> messages,
                        final Class<?> clazz,
                        final Type type,
                        final Annotation[] annotation,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final List<Message> messages,
                        final Class<?> clazz,
                        final Type type,
                        final Annotation[] annotation,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> arg5,
                        final OutputStream ostream) throws IOException, WebApplicationException {
        for (final Message m : messages) {
            ostream.write(m.toString().getBytes(MessageUtils.getCharset(mediaType)));
            final URI mUri = ui.get().getAbsolutePathBuilder().path(Integer.toString(m.getUniqueId())).build();
            ostream.write((" <a href='" + mUri.toASCIIString() + "'>link</a><br />").getBytes());
        }
    }
}
