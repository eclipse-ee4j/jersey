/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.wadl.internal;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.glassfish.jersey.server.internal.LocalizationMessages;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utils for wadl processing.
 *
 * @author Miroslav Fuksa
 *
 */
public class WadlUtils {


    /**
     * Name of the query parameter that allows generation of full WADL including
     * {@link org.glassfish.jersey.server.model.ExtendedResource extended resource}.
     */
    public static final String DETAILED_WADL_QUERY_PARAM = "detail";

    /**
     * Unmarshal a jaxb bean into a type of {@code resultClass} from the given {@code inputStream}.
     *
     * @param inputStream Input stream that contains input xml that should be processed.
     * @param saxParserFactory Sax parser factory for unmarshalling xml.
     * @param resultClass Class of the result bean into which the content of {@code inputStream} should be unmarshalled.
     * @param <T> Type of the result jaxb bean.
     * @return Unmarshalled jaxb bean.
     *
     * @throws JAXBException In case of jaxb problem.
     * @throws ParserConfigurationException In case of problem with parsing xml.
     * @throws SAXException In case of problem with parsing xml.
     */
    public static <T> T unmarshall(InputStream inputStream, SAXParserFactory saxParserFactory,
                                   Class<T> resultClass) throws JAXBException, ParserConfigurationException, SAXException {

        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(resultClass);
        } catch (JAXBException ex) {
            throw new ProcessingException(LocalizationMessages.ERROR_WADL_JAXB_CONTEXT(), ex);
        }

        final SAXParser saxParser = saxParserFactory.newSAXParser();
        SAXSource source = new SAXSource(saxParser.getXMLReader(), new InputSource(inputStream));
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final Object result = unmarshaller.unmarshal(source);
        return resultClass.cast(result);
    }

    /**
     * Return {@code true} if generation of full WADL with
     * {@link org.glassfish.jersey.server.model.ExtendedResource extended resources} is requested.
     *
     * @param uriInfo URI info of the request.
     * @return {@code true} if full detailed WADL should be generated; false otherwise.
     */
    public static boolean isDetailedWadlRequested(UriInfo uriInfo) {
        final List<String> simple = uriInfo.getQueryParameters().get(DETAILED_WADL_QUERY_PARAM);

        if (simple != null) {
            if (simple.size() == 0) {
                return true;
            }

            final String value = simple.get(0).trim();
            return value.isEmpty() || value.toUpperCase().equals("TRUE");
        }
        return false;
    }

}
