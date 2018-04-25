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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Base XML-based message body provider for collections of JAXB beans.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class XmlCollectionJaxbProvider extends AbstractCollectionJaxbProvider {

    private final Provider<XMLInputFactory> xif;

    XmlCollectionJaxbProvider(Provider<XMLInputFactory> xif, Providers ps) {
        super(ps);

        this.xif = xif;
    }

    XmlCollectionJaxbProvider(Provider<XMLInputFactory> xif, Providers ps, MediaType mt) {
        super(ps, mt);

        this.xif = xif;
    }

    /**
     * JAXB  provider for marshalling/un-marshalling collections
     * from/to entities of {@code application/xml} media type.
     */
    @Produces("application/xml")
    @Consumes("application/xml")
    @Singleton
    public static final class App extends XmlCollectionJaxbProvider {

        public App(@Context Provider<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps, MediaType.APPLICATION_XML_TYPE);
        }
    }

    /**
     * JAXB  provider for marshalling/un-marshalling collections
     * from/to entities of {@code text/xml} media type.
     */
    @Produces("text/xml")
    @Consumes("text/xml")
    @Singleton
    public static final class Text extends XmlCollectionJaxbProvider {

        public Text(@Context Provider<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps, MediaType.TEXT_XML_TYPE);
        }
    }

    /**
     * JAXB provider for marshalling/un-marshalling collections
     * from/to entities of {@code <type>/<sub-type>+xml} media types.
     */
    @Produces("*/*")
    @Consumes("*/*")
    @Singleton
    public static final class General extends XmlCollectionJaxbProvider {

        public General(@Context Provider<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    @Override
    protected final XMLStreamReader getXMLStreamReader(Class<?> elementType,
                                                       MediaType mediaType,
                                                       Unmarshaller u,
                                                       InputStream entityStream)
            throws XMLStreamException {
        return xif.get().createXMLStreamReader(entityStream);
    }

    @Override
    public final void writeCollection(Class<?> elementType, Collection<?> t,
                                      MediaType mediaType, Charset c,
                                      Marshaller m, OutputStream entityStream)
            throws JAXBException, IOException {
        final String rootElement = getRootElementName(elementType);
        final String cName = c.name();

        entityStream.write(
                String.format("<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>", cName).getBytes(cName));
        String property = "com.sun.xml.bind.xmlHeaders";
        String header;
        try {
            // standalone jaxb ri?
            header = (String) m.getProperty(property);
        } catch (PropertyException e) {
            // jaxb ri from jdk?
            property = "com.sun.xml.internal.bind.xmlHeaders";
            try {
                header = (String) m.getProperty(property);
            } catch (PropertyException ex) {
                // other jaxb implementation
                header = null;
                Logger.getLogger(XmlCollectionJaxbProvider.class.getName())
                        .log(Level.WARNING,
                                "@XmlHeader annotation is not supported with this JAXB implementation. Please use JAXB RI if "
                                        + "you need this feature.");
            }
        }
        if (header != null) {
            m.setProperty(property, "");
            entityStream.write(header.getBytes(cName));
        }
        entityStream.write(String.format("<%s>", rootElement).getBytes(cName));
        for (Object o : t) {
            m.marshal(o, entityStream);
        }

        entityStream.write(String.format("</%s>", rootElement).getBytes(cName));
    }
}
