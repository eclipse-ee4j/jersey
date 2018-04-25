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

package org.glassfish.jersey.jettison.internal.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.jersey.jaxb.internal.AbstractCollectionJaxbProvider;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonConfigured;
import org.glassfish.jersey.jettison.internal.Stax2JettisonFactory;

/**
 * JSON message entity media type provider (reader & writer) for collection
 * types.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JettisonListElementProvider extends AbstractCollectionJaxbProvider {

    JettisonListElementProvider(Providers ps) {
        super(ps);
    }

    JettisonListElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return super.isWriteable(type, genericType, annotations, mediaType);
    }

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JettisonListElementProvider {

        public App(@Context Providers ps) {
            super(ps, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JettisonListElementProvider {

        public General(@Context Providers ps) {
            super(ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    @Override
    public final void writeCollection(Class<?> elementType, Collection<?> t, MediaType mediaType, Charset c, Marshaller m,
                                      OutputStream entityStream) throws JAXBException, IOException {
        final OutputStreamWriter osw = new OutputStreamWriter(entityStream, c);

        JettisonConfig origJsonConfig = JettisonConfig.DEFAULT;
        if (m instanceof JettisonConfigured) {
            origJsonConfig = ((JettisonConfigured) m).getJSONConfiguration();
        }

        final JettisonConfig unwrappingJsonConfig = JettisonConfig.createJSONConfiguration(origJsonConfig);

        final XMLStreamWriter jxsw = Stax2JettisonFactory.createWriter(osw, unwrappingJsonConfig);
        final String invisibleRootName = getRootElementName(elementType);

        try {
            jxsw.writeStartDocument();
            jxsw.writeStartElement(invisibleRootName);
            for (Object o : t) {
                m.marshal(o, jxsw);
            }
            jxsw.writeEndElement();
            jxsw.writeEndDocument();
            jxsw.flush();
        } catch (XMLStreamException ex) {
            Logger.getLogger(JettisonListElementProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    @Override
    protected final XMLStreamReader getXMLStreamReader(Class<?> elementType, MediaType mediaType, Unmarshaller u,
                                                       InputStream entityStream) throws XMLStreamException {
        JettisonConfig c = JettisonConfig.DEFAULT;
        final Charset charset = getCharset(mediaType);
        if (u instanceof JettisonConfigured) {
            c = ((JettisonConfigured) u).getJSONConfiguration();
        }

        return Stax2JettisonFactory.createReader(new InputStreamReader(entityStream, charset), c);
    }
}
