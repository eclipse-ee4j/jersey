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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonConfigured;
import org.glassfish.jersey.jettison.JettisonMarshaller;

/**
 * Base JSON marshaller implementation class.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
public class BaseJsonMarshaller implements JettisonMarshaller, JettisonConfigured {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    protected final Marshaller jaxbMarshaller;
    protected JettisonConfig jsonConfig;

    public BaseJsonMarshaller(JAXBContext jaxbContext, JettisonConfig jsonConfig) throws JAXBException {
        this(jaxbContext.createMarshaller(), jsonConfig);
    }

    public BaseJsonMarshaller(Marshaller jaxbMarshaller, JettisonConfig jsonConfig) {
        this.jsonConfig = jsonConfig;
        this.jaxbMarshaller = jaxbMarshaller;
    }

    // JSONConfigurated

    public JettisonConfig getJSONConfiguration() {
        return jsonConfig;
    }

    // JSONMarshaller

    public void marshallToJSON(Object o, OutputStream outputStream) throws JAXBException {
        if (outputStream == null) {
            throw new IllegalArgumentException("The output stream is null");
        }

        marshallToJSON(o, new OutputStreamWriter(outputStream, UTF8));
    }

    public void marshallToJSON(Object o, Writer writer) throws JAXBException {
        if (o == null) {
            throw new IllegalArgumentException("The JAXB element is null");
        }

        if (writer == null) {
            throw new IllegalArgumentException("The writer is null");
        }

        jaxbMarshaller.marshal(o, getXMLStreamWriter(writer));
    }

    private XMLStreamWriter getXMLStreamWriter(Writer writer) throws JAXBException {
        try {
            return Stax2JettisonFactory.createWriter(writer, jsonConfig);
        } catch (IOException ex) {
            throw new JAXBException(ex);
        }
    }

    public void setProperty(String key, Object value) throws PropertyException {
        // do nothing
    }
}
