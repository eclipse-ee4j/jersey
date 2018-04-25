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

package org.glassfish.jersey.jaxb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.Providers;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.message.internal.EntityInputStream;

/**
 * An abstract provider for {@link JAXBElement}.
 * <p/>
 * Implementing classes may extend this class to provide specific marshalling
 * and unmarshalling behaviour.
 * <p/>
 * When unmarshalling a {@link UnmarshalException} will result in a
 * {@link WebApplicationException} being thrown with a status of 400
 * (Client error), and a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 * <p/>
 * When marshalling a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractJaxbElementProvider extends AbstractJaxbProvider<JAXBElement<?>> {

    /**
     * Inheritance constructor.
     *
     * @param providers JAX-RS providers.
     */
    public AbstractJaxbElementProvider(Providers providers) {
        super(providers);
    }

    /**
     * Inheritance constructor.
     *
     * @param providers         JAX-RS providers.
     * @param resolverMediaType JAXB component context resolver media type to be used.
     */
    public AbstractJaxbElementProvider(Providers providers, MediaType resolverMediaType) {
        super(providers, resolverMediaType);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == JAXBElement.class && genericType instanceof ParameterizedType && isSupported(mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JAXBElement.class.isAssignableFrom(type) && isSupported(mediaType);
    }

    @Override
    public final JAXBElement<?> readFrom(
            Class<JAXBElement<?>> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream inputStream) throws IOException {

        final EntityInputStream entityStream = EntityInputStream.create(inputStream);
        if (entityStream.isEmpty()) {
            throw new NoContentException(LocalizationMessages.ERROR_READING_ENTITY_MISSING());
        }

        final ParameterizedType pt = (ParameterizedType) genericType;
        final Class ta = (Class) pt.getActualTypeArguments()[0];

        try {
            return readFrom(ta, mediaType, getUnmarshaller(ta, mediaType), entityStream);
        } catch (UnmarshalException ex) {
            throw new BadRequestException(ex);
        } catch (JAXBException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Read JAXB element from an entity stream.
     *
     * @param type         the type that is to be read from the entity stream.
     * @param mediaType    the media type of the HTTP entity.
     * @param unmarshaller JAXB unmarshaller to be used.
     * @param entityStream the {@link InputStream} of the HTTP entity. The
     *                     caller is responsible for ensuring that the input stream ends when the
     *                     entity has been consumed. The implementation should not close the input
     *                     stream.
     * @return JAXB element representing the entity.
     * @throws JAXBException in case entity unmarshalling fails.
     */
    protected abstract JAXBElement<?> readFrom(
            Class<?> type, MediaType mediaType, Unmarshaller unmarshaller, InputStream entityStream) throws JAXBException;

    @Override
    public final void writeTo(
            JAXBElement<?> t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            final Marshaller m = getMarshaller(t.getDeclaredType(), mediaType);
            final Charset c = getCharset(mediaType);
            if (c != UTF8) {
                m.setProperty(Marshaller.JAXB_ENCODING, c.name());
            }
            setHeader(m, annotations);
            writeTo(t, mediaType, c, m, entityStream);
        } catch (JAXBException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Write JAXB element to an entity stream.
     *
     * @param element      JAXB element to be written to an entity stream.
     * @param mediaType    the media type of the HTTP entity.
     * @param charset      character set to be used.
     * @param marshaller   JAXB unmarshaller to be used.
     * @param entityStream the {@link InputStream} of the HTTP entity. The
     *                     caller is responsible for ensuring that the input stream ends when the
     *                     entity has been consumed. The implementation should not close the input
     *                     stream.
     * @throws JAXBException in case entity marshalling fails.
     */
    protected abstract void writeTo(JAXBElement<?> element,
                                    MediaType mediaType,
                                    Charset charset,
                                    Marshaller marshaller,
                                    OutputStream entityStream) throws JAXBException;
}
