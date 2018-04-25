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

package org.glassfish.jersey.jettison.internal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonConfigured;
import org.glassfish.jersey.jettison.JettisonUnmarshaller;

/**
 * Base JSON marshaller implementation class.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class BaseJsonUnmarshaller implements JettisonUnmarshaller, JettisonConfigured {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    protected final Unmarshaller jaxbUnmarshaller;
    protected final JettisonConfig jsonConfig;

    public BaseJsonUnmarshaller(JAXBContext jaxbContext, JettisonConfig jsonConfig) throws JAXBException {
        this(jaxbContext.createUnmarshaller(), jsonConfig);
    }

    public BaseJsonUnmarshaller(Unmarshaller jaxbUnmarshaller, JettisonConfig jsonConfig) {
        this.jaxbUnmarshaller = jaxbUnmarshaller;
        this.jsonConfig = jsonConfig;
    }

    // JsonConfigured
    public JettisonConfig getJSONConfiguration() {
        return jsonConfig;
    }

    // JsonUnmarshaller
    public <T> T unmarshalFromJSON(InputStream inputStream, Class<T> expectedType) throws JAXBException {
        return unmarshalFromJSON(new InputStreamReader(inputStream, UTF8), expectedType);
    }

    @SuppressWarnings("unchecked")
    public <T> T unmarshalFromJSON(Reader reader, Class<T> expectedType) throws JAXBException {
        if (!expectedType.isAnnotationPresent(XmlRootElement.class)) {
            return unmarshalJAXBElementFromJSON(reader, expectedType).getValue();
        } else {
            return (T) jaxbUnmarshaller.unmarshal(createXmlStreamReader(reader));
        }
    }

    public <T> JAXBElement<T> unmarshalJAXBElementFromJSON(InputStream inputStream, Class<T> declaredType) throws JAXBException {
        return unmarshalJAXBElementFromJSON(new InputStreamReader(inputStream, UTF8), declaredType);
    }

    public <T> JAXBElement<T> unmarshalJAXBElementFromJSON(Reader reader, Class<T> declaredType) throws JAXBException {
        return jaxbUnmarshaller.unmarshal(createXmlStreamReader(reader), declaredType);
    }

    private XMLStreamReader createXmlStreamReader(Reader reader) throws JAXBException {
        try {
            return Stax2JettisonFactory.createReader(reader, jsonConfig);
        } catch (XMLStreamException ex) {
            throw new UnmarshalException("Error creating JSON-based XMLStreamReader", ex);
        }
    }
}
