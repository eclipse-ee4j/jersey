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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Singleton;

/**
 * Provider for marshalling/un-marshalling of graphical image data represented as
 * {@code image/*, application/x-www-form-urlencoded} entity types to
 * {@link RenderedImage rendered} and from {@link RenderedImage rendered} or
 * {@link BufferedImage buffered} image instance.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Produces("image/*")
@Consumes({"image/*", "application/octet-stream"})
@Singleton
public final class RenderedImageProvider extends AbstractMessageReaderWriterProvider<RenderedImage> {

    private static final MediaType IMAGE_MEDIA_TYPE = new MediaType("image", "*");

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class == type || BufferedImage.class == type;
    }

    @Override
    public RenderedImage readFrom(
            Class<RenderedImage> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException {
        if (IMAGE_MEDIA_TYPE.isCompatible(mediaType)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mediaType.toString());
            if (!readers.hasNext()) {
                throw new IOException("The image-based media type " + mediaType + "is not supported for reading");
            }
            ImageReader reader = readers.next();

            ImageInputStream in = ImageIO.createImageInputStream(entityStream);
            reader.setInput(in, true, true);
            BufferedImage bi = reader.read(0, reader.getDefaultReadParam());
            in.close();
            reader.dispose();
            return bi;
        } else {
            return ImageIO.read(entityStream);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(
            RenderedImage t,
            Class<?> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        String formatName = getWriterFormatName(mediaType);
        if (formatName == null) {
            throw new IOException("The image-based media type " + mediaType + " is not supported for writing");
        }
        ImageIO.write(t, formatName, entityStream);
    }

    private String getWriterFormatName(MediaType t) {
        return getWriterFormatName(t.toString());
    }

    private String getWriterFormatName(String t) {
        Iterator<ImageWriter> i = ImageIO.getImageWritersByMIMEType(t);
        if (!i.hasNext()) {
            return null;
        }

        return i.next().getOriginatingProvider().getFormatNames()[0];
    }
}
