/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.validation.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.MessageUtils;
import org.glassfish.jersey.server.validation.ValidationErrorData;

/**
 * {@link MessageBodyWriter} providing support for (collections of) {@link ValidationErrorData}
 * that is able to output instances to {@code text/plain}/{@code text/html}.
 *
 * @author Michal Gajdos
 */
final class ValidationErrorMessageBodyWriter implements MessageBodyWriter<Object> {

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return isSupportedMediaType(mediaType) && isSupportedType(type, genericType);
    }

    private static boolean isSupportedType(final Class<?> type, final Type genericType) {
        if (ValidationErrorData.class.isAssignableFrom(type)) {
            return true;
        } else if (Collection.class.isAssignableFrom(type) && (genericType instanceof ParameterizedType)) {
            return ValidationErrorData.class
                    .isAssignableFrom((Class) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
        }
        return false;
    }

    private static boolean isSupportedMediaType(final MediaType mediaType) {
        return MediaType.TEXT_HTML_TYPE.equals(mediaType) || MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(final Object validationErrors,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Object entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        final Collection<ValidationErrorData> errors;

        if (entity instanceof ValidationErrorData) {
            errors = Collections.singleton((ValidationErrorData) entity);
        } else {
            //noinspection unchecked
            errors = (Collection<ValidationErrorData>) entity;
        }

        final boolean isPlain = MediaType.TEXT_PLAIN_TYPE.getSubtype().equals(mediaType.getSubtype());

        final StringBuilder builder = new StringBuilder();

        // Root <div>
        if (!isPlain) {
            builder.append("<div class=\"validation-errors\">");
        }

        for (final ValidationErrorData error : errors) {
            if (!isPlain) {
                builder.append("<div class=\"validation-error\">");
            }

            // Message.
            builder.append(isPlain ? error.getMessage() : "<span class=\"message\">" + error.getMessage() + "</span>");
            builder.append(' ');

            builder.append('(');

            // Path.
            builder.append(isPlain ? "path = " : ("<span class=\"path\"><strong>path</strong> = "));
            builder.append(isPlain ? error.getPath() : (error.getPath() + "</span>"));
            builder.append(',');
            builder.append(' ');

            // Invalid value.
            builder.append(isPlain ? "invalidValue = " : ("<span class=\"invalid-value\"><strong>invalidValue</strong> = "));
            builder.append(isPlain ? error.getInvalidValue()
                            : escapeHtml(error.getInvalidValue()).concat("</span>")
            );

            builder.append(')');

            if (!isPlain) {
                builder.append("</div>");
            } else {
                builder.append('\n');
            }
        }

        // Root <div>
        if (!isPlain) {
            builder.append("</div>");
        }

        entityStream.write(builder.toString().getBytes(MessageUtils.getCharset(mediaType)));
        entityStream.flush();
    }

    private static final String escapeHtml(String origin) {
        return origin == null ? ""
                : origin.replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }
}