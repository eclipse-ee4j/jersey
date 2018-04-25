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

package org.glassfish.jersey.jettison;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;

/**
 * A JSON marshaller responsible for serializing Java content trees, defined
 * by JAXB, to JSON data.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface JettisonMarshaller {

    public static final String FORMATTED = "org.glassfish.jersey.media.json.JsonMarshaller.formatted";

    /**
     * Marshall the content tree rooted at <code>jaxbElement</code> into an
     * output stream. The content tree may be an instance of a class that is
     * mapped to a XML root element (for example, annotated with
     * {@link javax.xml.bind.annotation.XmlRootElement}) or an instance of {@link javax.xml.bind.JAXBElement}.
     * <p>
     * The UTF-8 character encoding scheme will be used to encode the characters
     * of the JSON data.
     *
     * @param jaxbElement the root of the content tree to be marshalled.
     * @param os the JSON will be added to this stream.
     * @throws javax.xml.bind.JAXBException if any unexpected problem occurs during the
     *         marshalling.
     * @throws javax.xml.bind.MarshalException if the <code>JsonMarshaller</code> is unable to
     *         marshal <code>jaxbElement</code> (or any object reachable from obj)
     * @throws IllegalArgumentException if any of the method parameters are null.
     *
     */
    void marshallToJSON(Object jaxbElement, OutputStream os) throws JAXBException;

    /**
     * Marshall the content tree rooted at <code>jaxbElement</code> into an
     * output stream. The content tree may be an instance of a class that is
     * mapped to a XML root element (for example, annotated with
     * {@link javax.xml.bind.annotation.XmlRootElement}) or an instance of {@link javax.xml.bind.JAXBElement}.
     * <p>
     * The character encoding scheme of the <code>writer</code> will be used to
     * encode the characters of the JSON data.
     *
     * @param jaxbElement the root of the content tree to be marshalled.
     * @param writer the JSON will be added to this writer.
     * @throws javax.xml.bind.JAXBException if any unexpected problem occurs during the
     *         marshalling.
     * @throws javax.xml.bind.MarshalException if the <code>JsonMarshaller</code> is unable to
     *         marshal <code>jaxbElement</code> (or any object reachable from obj)
     * @throws IllegalArgumentException If any of the method parameters are null.
     */
    void marshallToJSON(Object jaxbElement, Writer writer) throws JAXBException;

    /**
     * Set the particular property in the underlying implementation of
     * {@link JettisonMarshaller}. Attempting to set an undefined property
     * will result in a PropertyException being thrown.
     *
     * @param name the name of the property to be set. This value can either
     *              be specified using one of the constant fields or a user
     *              supplied string.
     * @param value the value of the property to be set
     *
     * @throws javax.xml.bind.PropertyException when there is an error processing the given
     *                            property or value
     * @throws IllegalArgumentException
     *      If the name parameter is null
     */
    void setProperty(String name, Object value) throws PropertyException;
}
