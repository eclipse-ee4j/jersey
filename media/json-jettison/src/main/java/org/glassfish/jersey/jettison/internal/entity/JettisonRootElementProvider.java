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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.glassfish.jersey.jaxb.internal.AbstractRootElementJaxbProvider;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;

/**
 * JSON message entity media type provider (reader & writer) for JAXB types that
 * are annotated with {@link javax.xml.bind.annotation.XmlRootElement &#64;XmlRootElement}
 * or {@link javax.xml.bind.annotation.XmlType &#64;XmlType}.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JettisonRootElementProvider extends AbstractRootElementJaxbProvider {

    JettisonRootElementProvider(Providers ps) {
        super(ps);
    }

    JettisonRootElementProvider(Providers ps, MediaType mt) {
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
    public static final class App extends JettisonRootElementProvider {

        public App(@Context Providers ps) {
            super(ps, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JettisonRootElementProvider {

        public General(@Context Providers ps) {
            super(ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    @Override
    protected final Object readFrom(Class<Object> type, MediaType mediaType, Unmarshaller u,
                                    InputStream entityStream) throws JAXBException {
        final Charset c = getCharset(mediaType);

        return JettisonJaxbContext.getJSONUnmarshaller(u)
                .unmarshalFromJSON(new InputStreamReader(entityStream, c), type);
    }

    @Override
    protected void writeTo(Object t, MediaType mediaType, Charset c, Marshaller m, OutputStream entityStream) throws
            JAXBException {
        JettisonMarshaller jsonMarshaller = JettisonJaxbContext.getJSONMarshaller(m);
        if (isFormattedOutput()) {
            jsonMarshaller.setProperty(JettisonMarshaller.FORMATTED, true);
        }
        jsonMarshaller.marshallToJSON(t, new OutputStreamWriter(entityStream, c));
    }
}
