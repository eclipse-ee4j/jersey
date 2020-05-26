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

import javax.xml.parsers.SAXParserFactory;
import java.util.Collections;
import java.util.Map;

/** This supplier is used to set the features on the instances of the supported classes:
 * <p><ul>
 *     <li>{@link javax.xml.parsers.SAXParserFactory}</li>
 *     <li>{@link javax.xml.transform.TransformerFactory}</li>
 * </ul></p> using one of the methods:
 * <p><ul>
 *     <li>{@link javax.xml.parsers.SAXParserFactory#setFeature(String, boolean)}</li>
 *     <li>{@link javax.xml.transform.TransformerFactory#setFeature(String, boolean)}</li>
 * </ul></p>
 *
 * @since 2.31
 */
@Contract
public interface FeatureSupplier {

    /**
     * Define whether the feature set is for the instances of the given class.
     * @param factoryClass the class for which instance the feature set is to be applied.
     * @return true if this contract implementation is for the given class.
     */
    boolean isFor(Class<?> factoryClass);

    /**
     * The feature set to be applied.
     * @return the feature set {@code Map} with keys and {@code Boolean} values.
     */
    Map<String, Boolean> getFeatures();

    /**
     * Supply a feature that disables disallow-doctype-decl feature and allows the ENTITY in the xml DOCTYPE.
     * Registering this feature will override the settings of the secure {@link SAXParserFactory}.
     * @return A feature that sets {@code http://apache.org/xml/features/disallow-doctype-decl} feature to false.
     */
    static FeatureSupplier allowDoctypeDeclFeature() {
        return new FeatureSupplier() {
            @Override
            public boolean isFor(Class<?> factoryClass) {
                return SAXParserFactory.class == factoryClass;
            }

            @Override
            public Map<String, Boolean> getFeatures() {
                return Collections.singletonMap("http://apache.org/xml/features/disallow-doctype-decl", false);
            }
        };
    }
}
