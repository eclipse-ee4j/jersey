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
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.Providers;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.message.internal.EntityInputStream;

/**
 * An abstract provider for JAXB types that are annotated with
 * {@link XmlRootElement} or {@link XmlType}.
 * <p>
 * Implementing classes may extend this class to provide specific marshalling
 * and unmarshalling behaviour.
 * <p>
 * When unmarshalling a {@link UnmarshalException} will result in a
 * {@link WebApplicationException} being thrown with a status of 400
 * (Client error), and a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 * <p>
 * When marshalling a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractRootElementJaxbProvider extends AbstractJaxbProvider<Object> {

    /**
     * Inheritance constructor.
     *
     * @param providers JAX-RS providers.
     */
    public AbstractRootElementJaxbProvider(Providers providers) {
        super(providers);
    }

    /**
     * Inheritance constructor.
     *
     * @param providers JAX-RS providers.
     * @param resolverMediaType JAXB component context resolver media type to be used.
     */
    public AbstractRootElementJaxbProvider(Providers providers, MediaType resolverMediaType) {
        super(providers, resolverMediaType);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (type.getAnnotation(XmlRootElement.class) != null
                || type.getAnnotation(XmlType.class) != null) && isSupported(mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getAnnotation(XmlRootElement.class) != null && isSupported(mediaType);
    }

    @Override
    public final Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream inputStream) throws IOException {

        try {
            final EntityInputStream entityStream = EntityInputStream.create(inputStream);
            if (entityStream.isEmpty()) {
                throw new NoContentException(LocalizationMessages.ERROR_READING_ENTITY_MISSING());
            }
            return readFrom(type, mediaType, getUnmarshaller(type, mediaType), entityStream);
        } catch (UnmarshalException ex) {
            throw new BadRequestException(ex);
        } catch (JAXBException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    /**
     * Unmarshal a JAXB type.
     * <p>
     * Implementing classes may override this method.
     *
     * @param type         the JAXB type
     * @param mediaType    the media type
     * @param u            the unmarshaller to use for unmarshalling.
     * @param entityStream the input stream to unmarshal from.
     * @return an instance of the JAXB type.
     * @throws javax.xml.bind.JAXBException in case the JAXB unmarshalling fails.
     */
    protected Object readFrom(Class<Object> type, MediaType mediaType,
                              Unmarshaller u, InputStream entityStream)
            throws JAXBException {
        if (type.isAnnotationPresent(XmlRootElement.class)) {
            return u.unmarshal(entityStream);
        } else {
            return u.unmarshal(new StreamSource(entityStream), type).getValue();
        }
    }

    @Override
    public final void writeTo(
            Object t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            final Marshaller m = getMarshaller(type, mediaType);
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
     * Marshal an instance of a JAXB type.
     * <p>
     * Implementing classes may override this method.
     *
     * @param t            the instance of the JAXB type.
     * @param mediaType    the media type.
     * @param c            the character set to serialize characters to.
     * @param m            the marshaller to marshaller the instance of the JAXB type.
     * @param entityStream the output stream to marshal to.
     * @throws javax.xml.bind.JAXBException in case the JAXB marshalling fails.
     */
    protected void writeTo(Object t, MediaType mediaType, Charset c,
                           Marshaller m, OutputStream entityStream)
            throws JAXBException {
        m.marshal(t, entityStream);
    }
}
