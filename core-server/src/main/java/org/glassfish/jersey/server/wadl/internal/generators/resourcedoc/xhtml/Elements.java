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

package org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.xhtml;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * This class provides a fluent interface to xhtml supporting jaxb bindings.<br>
 * Created on: Jun 17, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class Elements extends JAXBElement<XhtmlElementType> {

    public static Elements el(String elementName) {
        return createElement(elementName);
    }

    public static Object val(String elementName, String value) {
        return createElement(elementName, value);
    }

    private static final long serialVersionUID = 1L;

    public Elements(QName name, Class<XhtmlElementType> clazz,
                    XhtmlElementType element) {
        super(name, clazz, element);
    }

    public Elements add(Object... childNodes) {
        if (childNodes != null) {
            for (Object childNode : childNodes) {
                getValue().getChildNodes().add(childNode);
            }
        }
        return this;
    }

    public Elements addChild(Object child) {
        getValue().getChildNodes().add(child);
        return this;
    }

    private static Elements createElement(final String elementName) {
        try {

            final XhtmlElementType element = new XhtmlElementType();

            final Elements jaxbElement = new Elements(
                    new QName("http://www.w3.org/1999/xhtml", elementName),
                    XhtmlElementType.class,
                    element);

            return jaxbElement;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JAXBElement<XhtmlValueType> createElement(final String elementName, String value) {
        try {

            final XhtmlValueType element = new XhtmlValueType();
            element.value = value;

            final JAXBElement<XhtmlValueType> jaxbElement = new JAXBElement<XhtmlValueType>(
                    new QName("http://www.w3.org/1999/xhtml", elementName),
                    XhtmlValueType.class,
                    element);

            return jaxbElement;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
