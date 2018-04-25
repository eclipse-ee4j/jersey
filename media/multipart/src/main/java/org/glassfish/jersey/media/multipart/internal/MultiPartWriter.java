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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import javax.inject.Singleton;

import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.message.MessageUtils;

/**
 * {@link Provider} {@link MessageBodyWriter} implementation for {@link MultiPart} entities.
 *
 * @author Craig McClanahan
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
@Singleton
@Produces("multipart/*")
public class MultiPartWriter implements MessageBodyWriter<MultiPart> {

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    /**
     * Injectable helper to look up appropriate {@link Provider}s
     * for our body parts.
     */
    private final Providers providers;

    public MultiPartWriter(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public long getSize(final MultiPart entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return MultiPart.class.isAssignableFrom(type);
    }

    /**
     * Write the entire list of body parts to the output stream, using the
     * appropriate provider implementation to serialize each body part's entity.
     *
     * @param entity      the {@link MultiPart} instance to write.
     * @param type        the class of the object to be written (i.e. {@link MultiPart}.class).
     * @param genericType the type of object to be written.
     * @param annotations annotations on the resource method that returned this object.
     * @param mediaType   media type ({@code multipart/*}) of this entity.
     * @param headers     mutable map of HTTP headers for the entire response.
     * @param stream      output stream to which the entity should be written.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws javax.ws.rs.WebApplicationException
     *                             if an HTTP error response
     *                             needs to be produced (only effective if the response is not committed yet).
     */
    @Override
    public void writeTo(final MultiPart entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> headers,
                        final OutputStream stream) throws IOException, WebApplicationException {

        // Verify that there is at least one body part.
        if ((entity.getBodyParts() == null) || (entity.getBodyParts().size() < 1)) {
            throw new IllegalArgumentException(LocalizationMessages.MUST_SPECIFY_BODY_PART());
        }

        // If our entity is not nested, make sure the MIME-Version header is set.
        if (entity.getParent() == null) {
            final Object value = headers.getFirst("MIME-Version");
            if (value == null) {
                headers.putSingle("MIME-Version", "1.0");
            }
        }

        // Initialize local variables we need.
        final Writer writer = new BufferedWriter(new OutputStreamWriter(stream, MessageUtils.getCharset(mediaType)));

        // Determine the boundary string to be used, creating one if needed.
        final MediaType boundaryMediaType = Boundary.addBoundary(mediaType);
        if (boundaryMediaType != mediaType) {
            headers.putSingle(HttpHeaders.CONTENT_TYPE, boundaryMediaType.toString());
        }

        final String boundaryString = boundaryMediaType.getParameters().get("boundary");

        // Iterate through the body parts for this message.
        boolean isFirst = true;
        for (final BodyPart bodyPart : entity.getBodyParts()) {

            // Write the leading boundary string
            if (isFirst) {
                isFirst = false;
                writer.write("--");
            } else {
                writer.write("\r\n--");
            }
            writer.write(boundaryString);
            writer.write("\r\n");

            // Write the headers for this body part
            final MediaType bodyMediaType = bodyPart.getMediaType();
            if (bodyMediaType == null) {
                throw new IllegalArgumentException(LocalizationMessages.MISSING_MEDIA_TYPE_OF_BODY_PART());
            }

            final MultivaluedMap<String, String> bodyHeaders = bodyPart.getHeaders();
            bodyHeaders.putSingle("Content-Type", bodyMediaType.toString());

            if (bodyHeaders.getFirst("Content-Disposition") == null && bodyPart.getContentDisposition() != null) {
                bodyHeaders.putSingle("Content-Disposition", bodyPart.getContentDisposition().toString());
            }

            // Iterate for the nested body parts
            for (final Map.Entry<String, List<String>> entry : bodyHeaders.entrySet()) {
                // Write this header and its value(s)
                writer.write(entry.getKey());
                writer.write(':');
                boolean first = true;
                for (final String value : entry.getValue()) {
                    if (first) {
                        writer.write(' ');
                        first = false;
                    } else {
                        writer.write(',');
                    }
                    writer.write(value);
                }
                writer.write("\r\n");
            }

            // Mark the end of the headers for this body part
            writer.write("\r\n");
            writer.flush();

            // Write the entity for this body part
            Object bodyEntity = bodyPart.getEntity();
            if (bodyEntity == null) {
                throw new IllegalArgumentException(LocalizationMessages.MISSING_ENTITY_OF_BODY_PART(bodyMediaType));
            }

            Class bodyClass = bodyEntity.getClass();
            if (bodyEntity instanceof BodyPartEntity) {
                bodyClass = InputStream.class;
                bodyEntity = ((BodyPartEntity) bodyEntity).getInputStream();
            }

            final MessageBodyWriter bodyWriter = providers.getMessageBodyWriter(
                    bodyClass,
                    bodyClass,
                    EMPTY_ANNOTATIONS,
                    bodyMediaType);

            if (bodyWriter == null) {
                throw new IllegalArgumentException(LocalizationMessages.NO_AVAILABLE_MBW(bodyClass, mediaType));
            }

            bodyWriter.writeTo(
                    bodyEntity,
                    bodyClass,
                    bodyClass,
                    EMPTY_ANNOTATIONS,
                    bodyMediaType,
                    bodyHeaders,
                    stream
            );
        }

        // Write the final boundary string
        writer.write("\r\n--");
        writer.write(boundaryString);
        writer.write("--\r\n");
        writer.flush();
    }

}
