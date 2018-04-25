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

import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

/**
 * A JSON unmarshaller responsible for deserializing JSON data to a Java
 * content tree, defined by JAXB.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface JettisonUnmarshaller {

    /**
     * Unmarshal JSON data from the specified <code>InputStream</code> and
     * return the resulting Java content tree.
     * <p>
     * The UTF-8 character encoding scheme will be used to decode the encoded
     * characters of the JSON data.
     *
     * @param <T> the type of the Java content tree.
     * @param is the InputStream to unmarshal JSON data from.
     * @param expectedType the expected type of the Java content tree.
     * @return the newly created root object of the Java content tree. The
     *         content tree may be an instance of a class that is
     *         mapped to a XML root element (for example, annotated with
     *         {@link javax.xml.bind.annotation.XmlRootElement}) or mapped to an XML type (for example,
     *         annotated with {@link javax.xml.bind.annotation.XmlType}).
     * @throws javax.xml.bind.JAXBException if any unexpected errors occur while unmarshalling.
     * @throws javax.xml.bind.UnmarshalException if the <code>JsonUnmarshaller</code> is unable
     *         to perform the JSON to Java binding.
     */
    <T> T unmarshalFromJSON(InputStream is, Class<T> expectedType) throws JAXBException;

    /**
     * Unmarshal JSON data from the specified <code>Reader</code> and
     * return the resulting Java content tree.
     * <p>
     * The character encoding scheme of the <code>reader</code> will be used to
     * encode the characters of the JSON data.
     *
     * @param <T> the type of the Java content tree.
     * @param reader the Reader to unmarshal JSON data from.
     * @param expectedType the expected type of the Java content tree.
     * @return the newly created root object of the Java content tree. The
     *         content tree may be an instance of a class that is
     *         mapped to a XML root element (for example, annotated with
     *         {@link javax.xml.bind.annotation.XmlRootElement}) or mapped to an XML type (for example,
     *         annotated with {@link javax.xml.bind.annotation.XmlType}).
     * @throws javax.xml.bind.JAXBException if any unexpected errors occur while unmarshalling.
     * @throws javax.xml.bind.UnmarshalException if the <code>JsonUnmarshaller</code> is unable
     *         to perform the JSON to Java binding.
     */
    <T> T unmarshalFromJSON(Reader reader, Class<T> expectedType) throws JAXBException;

    /**
     * Unmarshal JSON data from the <code>InputStream</code> by
     * <code>declaredType</code> and return the resulting content tree.
     * <p>
     * The UTF-8 character encoding scheme will be used to decode the encoded
     * characters of the JSON data.
     *
     * @param <T> the type of the Java content tree.
     * @param is the InputStream to unmarshal JSON data from.
     * @param declaredType a class that is mapped to a XML root element
     *        (for example, annotated with {@link javax.xml.bind.annotation.XmlRootElement}) or mapped to
     *        an XML type (for example, annotated with {@link javax.xml.bind.annotation.XmlType}).
     * @return the newly created root object of the Java content tree, root
     *         by a {@link javax.xml.bind.JAXBElement} instance.
     * @throws javax.xml.bind.JAXBException if any unexpected errors occur while unmarshalling.
     * @throws javax.xml.bind.UnmarshalException if the <code>JsonUnmarshaller</code> is unable
     *         to perform the JSON to Java binding.
     */
    <T> JAXBElement<T> unmarshalJAXBElementFromJSON(InputStream is, Class<T> declaredType) throws JAXBException;

    /**
     * Unmarshal JSON data from the <code>Reader</code> by
     * <code>declaredType</code> and return the resulting content tree.
     * <p>
     * The character encoding scheme of the <code>reader</code> will be used to
     * encode the characters of the JSON data.
     *
     * @param <T> the type of the Java content tree.
     * @param reader the Reader to unmarshal JSON data from.
     * @param declaredType a class that is mapped to a XML root element
     *        (for example, annotated with {@link javax.xml.bind.annotation.XmlRootElement}) or mapped to
     *        an XML type (for example, annotated with {@link javax.xml.bind.annotation.XmlType}).
     * @return the newly created root object of the Java content tree, root
     *         by a {@link javax.xml.bind.JAXBElement} instance.
     * @throws javax.xml.bind.JAXBException if any unexpected errors occur while unmarshalling.
     * @throws javax.xml.bind.UnmarshalException if the <code>JsonUnmarshaller</code> is unable
     *         to perform the JSON to Java binding.
     */
    <T> JAXBElement<T> unmarshalJAXBElementFromJSON(Reader reader, Class<T> declaredType) throws JAXBException;
}
