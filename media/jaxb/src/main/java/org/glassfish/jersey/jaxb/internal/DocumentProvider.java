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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provider for marshalling/un-marshalling {@link Document XML document} instances.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Produces({"application/xml", "text/xml", "*/*"})
@Consumes({"application/xml", "text/xml", "*/*"})
@Singleton
public final class DocumentProvider extends AbstractMessageReaderWriterProvider<Document> {

    @Inject
    private Provider<DocumentBuilderFactory> dbf;
    @Inject
    private Provider<TransformerFactory> tf;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Document.class == type;
    }

    @Override
    public Document readFrom(
            Class<Document> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException {
        try {
            return dbf.get().newDocumentBuilder().parse(entityStream);
        } catch (SAXException ex) {
            throw new BadRequestException(ex);
        } catch (ParserConfigurationException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Document.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(
            Document t,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            StreamResult sr = new StreamResult(entityStream);
            tf.get().newTransformer().transform(new DOMSource(t), sr);
        } catch (TransformerException ex) {
            throw new InternalServerErrorException(ex);
        }
    }
}
