/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb;

import org.glassfish.jersey.spi.Contract;

import java.util.Map;


/** This supplier is used to set the properties on the instances of the supported classes:
 * <p><ul>
 *     <li>{@link javax.xml.parsers.DocumentBuilderFactory}</li>
 *     <li>{@link javax.xml.parsers.SAXParser}</li>
 *     <li>{@link javax.xml.stream.XMLInputFactory}</li>
 *     <li>{@link javax.xml.transform.TransformerFactory}</li>
 * </ul></p> using of the methods
 * <p><ul>
 *     <li>{@link javax.xml.parsers.DocumentBuilderFactory#setAttribute(String, Object)}</li>
 *     <li>{@link javax.xml.parsers.SAXParser#setProperty(String, Object)}</li>
 *     <li>{@link javax.xml.stream.XMLInputFactory#setProperty(String, Object)}</li>
 *     <li>{@link javax.xml.transform.TransformerFactory#setAttribute(String, Object)}</li>
 * </ul></p>
 *
 * @since 2.31
 */
@Contract
public interface PropertySupplier {
    /**
     * Define whether the property set is for the instances of the given class.
     * @param factoryOrParserClass the class for which instance the property set is to be applied.
     * @return true if this contract implementation is for given class.
     */
    boolean isFor(Class<?> factoryOrParserClass);

    /**
     * The properties to be applied.
     * @return the property {@code Map} with keys and {@code Object} values to be applied.
     */
    Map<String, Object> getProperties();
}
