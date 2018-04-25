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

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.Providers;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.glassfish.jersey.message.internal.EntityInputStream;

/**
 * Base XML-based message body reader for JAXB beans.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class XmlRootObjectJaxbProvider extends AbstractJaxbProvider<Object> {

    private final Provider<SAXParserFactory> spf;

    XmlRootObjectJaxbProvider(Provider<SAXParserFactory> spf, Providers ps) {
        super(ps);

        this.spf = spf;
    }

    XmlRootObjectJaxbProvider(Provider<SAXParserFactory> spf, Providers ps, MediaType mt) {
        super(ps, mt);

        this.spf = spf;
    }

    @Override
    protected JAXBContext getStoredJaxbContext(Class type) throws JAXBException {
        return null;
    }

    /**
     * Provider for un-marshalling entities of {@code application/xml} media type
     * into JAXB beans using {@link Unmarshaller JAXB unmarshaller}.
     */
    @Produces("application/xml")
    @Consumes("application/xml")
    @Singleton
    public static final class App extends XmlRootObjectJaxbProvider {

        public App(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.APPLICATION_XML_TYPE);
        }
    }

    /**
     * Provider for un-marshalling entities of {@code text/xml} media type
     * into JAXB beans using {@link Unmarshaller JAXB unmarshaller}.
     */
    @Produces("text/xml")
    @Consumes("text/xml")
    @Singleton
    public static final class Text extends XmlRootObjectJaxbProvider {

        public Text(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.TEXT_XML_TYPE);
        }
    }

    /**
     * Provider for un-marshalling entities of {@code <type>/<sub-type>+xml} media types
     * into JAXB beans using {@link Unmarshaller JAXB unmarshaller}.
     */
    @Produces("*/*")
    @Consumes("*/*")
    @Singleton
    public static final class General extends XmlRootObjectJaxbProvider {

        public General(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        try {
            return Object.class == type && isSupported(mediaType) && getUnmarshaller(type, mediaType) != null;
        } catch (JAXBException cause) {
            throw new RuntimeException(LocalizationMessages.ERROR_UNMARSHALLING_JAXB(type), cause);
        }
    }

    @Override
    public final Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream inputStream) throws IOException {

        final EntityInputStream entityStream = EntityInputStream.create(inputStream);
        if (entityStream.isEmpty()) {
            throw new NoContentException(LocalizationMessages.ERROR_READING_ENTITY_MISSING());
        }

        try {
            return getUnmarshaller(type, mediaType)
                    .unmarshal(getSAXSource(spf.get(), entityStream));
        } catch (UnmarshalException ex) {
            throw new BadRequestException(ex);
        } catch (JAXBException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
        return false;
    }

    @Override
    public void writeTo(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3,
                        MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6)
            throws IOException, WebApplicationException {
        throw new IllegalArgumentException();
    }
}
