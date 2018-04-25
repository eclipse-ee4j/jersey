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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;

import org.glassfish.jersey.jettison.JettisonConfig;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * JSON JAXB marshaller.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class JettisonJaxbMarshaller extends BaseJsonMarshaller implements Marshaller {

    public JettisonJaxbMarshaller(JAXBContext jaxbContext, JettisonConfig jsonConfig) throws JAXBException {
        super(jaxbContext, jsonConfig);
    }

    // Marshaller
    @Override
    public void marshal(Object jaxbObject, Result result) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, result);
    }

    @Override
    public void marshal(Object jaxbObject, OutputStream os) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, os);
    }

    @Override
    public void marshal(Object jaxbObject, File file) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, file);
    }

    @Override
    public void marshal(Object jaxbObject, Writer writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    @Override
    public void marshal(Object jaxbObject, ContentHandler handler) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, handler);
    }

    @Override
    public void marshal(Object jaxbObject, Node node) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, node);
    }

    @Override
    public void marshal(Object jaxbObject, XMLStreamWriter writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    @Override
    public void marshal(Object jaxbObject, XMLEventWriter writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    @Override
    public Node getNode(Object jaxbObject) throws JAXBException {
        return jaxbMarshaller.getNode(jaxbObject);
    }

    @Override
    public void setProperty(String name, Object value) throws PropertyException {
        if (name == null) {
            throw new IllegalArgumentException("Name can't be null.");
        }

        if (name.equals(org.glassfish.jersey.jettison.JettisonMarshaller.FORMATTED)) {
            if (!(value instanceof Boolean)) {
                throw new PropertyException("property " + name + " must be an instance of type "
                        + "boolean, not " + value.getClass().getName());
            }

            jsonConfig = JettisonConfig.createJSONConfiguration(jsonConfig);
        } else {
            jaxbMarshaller.setProperty(name, value);
        }
    }

    @Override
    public Object getProperty(String key) throws PropertyException {
        return jaxbMarshaller.getProperty(key);
    }

    @Override
    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        jaxbMarshaller.setEventHandler(handler);
    }

    @Override
    public ValidationEventHandler getEventHandler() throws JAXBException {
        return jaxbMarshaller.getEventHandler();
    }

    @Override
    public void setAdapter(XmlAdapter adapter) {
        jaxbMarshaller.setAdapter(adapter);
    }

    @Override
    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        jaxbMarshaller.setAdapter(type, adapter);
    }

    @Override
    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        return jaxbMarshaller.getAdapter(type);
    }

    @Override
    public void setAttachmentMarshaller(AttachmentMarshaller marshaller) {
        jaxbMarshaller.setAttachmentMarshaller(marshaller);
    }

    @Override
    public AttachmentMarshaller getAttachmentMarshaller() {
        return jaxbMarshaller.getAttachmentMarshaller();
    }

    @Override
    public void setSchema(Schema schema) {
        jaxbMarshaller.setSchema(schema);
    }

    @Override
    public Schema getSchema() {
        return jaxbMarshaller.getSchema();
    }

    @Override
    public void setListener(Listener listener) {
        jaxbMarshaller.setListener(listener);
    }

    @Override
    public Listener getListener() {
        return jaxbMarshaller.getListener();
    }
}
