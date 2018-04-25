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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Collection of {@link Source XML source} providers.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public final class SourceProvider {

    /**
     * Provider for un-marshalling {@link StreamSource} instances.
     */
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    @Singleton
    public static final class StreamSourceReader implements MessageBodyReader<StreamSource> {

        @Override
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return StreamSource.class == t || Source.class == t;
        }

        @Override
        public StreamSource readFrom(
                Class<StreamSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return new StreamSource(entityStream);
        }
    }

    /**
     * Provider for un-marshalling {@link SAXSource} instances.
     */
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    @Singleton
    public static final class SaxSourceReader implements MessageBodyReader<SAXSource> {
        // Delay construction of factory

        private final Provider<SAXParserFactory> spf;

        public SaxSourceReader(@Context Provider<SAXParserFactory> spf) {
            this.spf = spf;
        }

        @Override
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return SAXSource.class == t;
        }

        @Override
        public SAXSource readFrom(
                Class<SAXSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                return new SAXSource(spf.get().newSAXParser().getXMLReader(),
                        new InputSource(entityStream));
            } catch (SAXParseException ex) {
                throw new BadRequestException(ex);
            } catch (SAXException ex) {
                throw new InternalServerErrorException(ex);
            } catch (ParserConfigurationException ex) {
                throw new InternalServerErrorException(ex);
            }
        }
    }

    /**
     * Provider for marshalling/un-marshalling {@link DOMSource} instances.
     */
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    @Singleton
    public static final class DomSourceReader implements MessageBodyReader<DOMSource> {

        private final Provider<DocumentBuilderFactory> dbf;

        public DomSourceReader(@Context Provider<DocumentBuilderFactory> dbf) {
            this.dbf = dbf;
        }

        @Override
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return DOMSource.class == t;
        }

        @Override
        public DOMSource readFrom(
                Class<DOMSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                Document d = dbf.get().newDocumentBuilder().parse(entityStream);
                return new DOMSource(d);
            } catch (SAXParseException ex) {
                throw new BadRequestException(ex);
            } catch (SAXException ex) {
                throw new InternalServerErrorException(ex);
            } catch (ParserConfigurationException ex) {
                throw new InternalServerErrorException(ex);
            }
        }
    }

    /**
     * Provider for marshaling {@link Source} instances.
     */
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    @Singleton
    public static final class SourceWriter implements MessageBodyWriter<Source> {

        private final Provider<SAXParserFactory> saxParserFactory;
        private final Provider<TransformerFactory> transformerFactory;

        public SourceWriter(@Context Provider<SAXParserFactory> spf,
                @Context Provider<TransformerFactory> tf) {
            this.saxParserFactory = spf;
            this.transformerFactory = tf;
        }

        @Override
        public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return Source.class.isAssignableFrom(t);
        }

        @Override
        public long getSize(Source o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Source source, Class<?> t, Type gt, Annotation[] as, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

            try {
                if (source instanceof StreamSource) {
                    StreamSource stream = (StreamSource) source;
                    InputSource inputStream = new InputSource(stream.getInputStream());
                    inputStream.setCharacterStream(inputStream.getCharacterStream());
                    inputStream.setPublicId(stream.getPublicId());
                    inputStream.setSystemId(source.getSystemId());
                    source = new SAXSource(saxParserFactory.get().newSAXParser().getXMLReader(), inputStream);
                }

                StreamResult sr = new StreamResult(entityStream);
                transformerFactory.get().newTransformer().transform(source, sr);

            } catch (SAXException ex) {
                throw new InternalServerErrorException(ex);
            } catch (ParserConfigurationException ex) {
                throw new InternalServerErrorException(ex);
            } catch (TransformerException ex) {
                throw new InternalServerErrorException(ex);
            }
        }
    }
}
