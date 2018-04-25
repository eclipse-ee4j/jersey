/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import java.security.AccessController;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

/**
 * Common helper methods for SAX parsers.
 *
 * @author Michal Gajdos
 */
public final class SaxHelper {

    private SaxHelper() {
    }

    /**
     * Determines whether the given SAX {@code parserFactory} is from XDK package or not.
     *
     * @param parserFactory SAX parser factory to evaluate.
     * @return {@code true} if the given SAX parser factory is an XDK factory, {@code false} otherwise.
     */
    public static boolean isXdkParserFactory(final SAXParserFactory parserFactory) {
        return isXdkFactory(parserFactory, "oracle.xml.jaxp.JXSAXParserFactory");
    }

    /**
     * Determines whether the given {@code builderFactory} is from XDK package or not.
     *
     * @param builderFactory document builder factory to evaluate.
     * @return {@code true} if the given document builder factory is an XDK factory, {@code false} otherwise.
     */
    public static boolean isXdkDocumentBuilderFactory(final DocumentBuilderFactory builderFactory) {
        return isXdkFactory(builderFactory, "oracle.xml.jaxp.JXDocumentBuilderFactory");
    }

    private static boolean isXdkFactory(final Object factory, final String className) {
        final Class<?> xdkFactoryClass = AccessController.doPrivileged(ReflectionHelper.classForNamePA(className, null));
        if (xdkFactoryClass == null) {
            return false;
        }
        return xdkFactoryClass.isAssignableFrom(factory.getClass());
    }
}
