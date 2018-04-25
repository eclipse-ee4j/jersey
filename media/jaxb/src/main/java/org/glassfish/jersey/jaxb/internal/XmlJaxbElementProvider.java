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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

/**
 * Base XML-based message body provider for {@link JAXBElement JAXB element} instances.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class XmlJaxbElementProvider extends AbstractJaxbElementProvider {

    private final Provider<SAXParserFactory> spf;

    public XmlJaxbElementProvider(Provider<SAXParserFactory> spf, Providers ps) {
        super(ps);

        this.spf = spf;
    }

    public XmlJaxbElementProvider(Provider<SAXParserFactory> spf, Providers ps, MediaType mt) {
        super(ps, mt);

        this.spf = spf;
    }

    /**
     * Provider for marshalling/un-marshalling {@link JAXBElement JAXB elements}
     * from/to entities of {@code application/xml} media type.
     */
    @Produces("application/xml")
    @Consumes("application/xml")
    @Singleton
    public static final class App extends XmlJaxbElementProvider {

        public App(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.APPLICATION_XML_TYPE);
        }
    }

    /**
     * Provider for marshalling/un-marshalling {@link JAXBElement JAXB elements}
     * from/to entities of {@code text/xml} media type.
     */
    @Produces("text/xml")
    @Consumes("text/xml")
    @Singleton
    public static final class Text extends XmlJaxbElementProvider {

        public Text(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.TEXT_XML_TYPE);
        }
    }

    /**
     * Provider for marshalling/un-marshalling {@link JAXBElement JAXB elements}
     * from/to entities of {@code <type>/<sub-type>+xml} media types.
     */
    @Produces("*/*,*/*+xml")
    @Consumes("*/*,*/*+xml")
    @Singleton
    public static final class General extends XmlJaxbElementProvider {

        public General(@Context Provider<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    @Override
    protected final JAXBElement<?> readFrom(Class<?> type, MediaType mediaType,
                                            Unmarshaller unmarshaller, InputStream entityStream) throws JAXBException {
        return unmarshaller.unmarshal(getSAXSource(spf.get(), entityStream), type);
    }

    @Override
    protected final void writeTo(JAXBElement<?> t, MediaType mediaType, Charset c,
                                 Marshaller m, OutputStream entityStream) throws JAXBException {
        m.marshal(t, entityStream);
    }
}
