/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.jaxb.FeatureSupplier;

import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-scoped injection provider of {@link SAXParserFactory SAX parser factories}.
 *
 * If {@link org.glassfish.jersey.message.MessageProperties#XML_SECURITY_DISABLE} is not set,
 * the {@link SecureSaxParserFactory} is returned. By default, the {@code http://apache.org/xml/features/disallow-doctype-decl}
 * feature is set to {@code TRUE}. To override this settings, it is possible to register the
 * {@link FeatureSupplier#allowDoctypeDeclFeature()}.
 *
 * @see FeatureSupplier
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 * @author Martin Matula
 */
public class SaxParserFactoryInjectionProvider extends AbstractXmlFactory<SAXParserFactory> {

    /**
     * Create new SAX parser factory provider.
     *
     * @param config Jersey configuration properties.
     */
    // TODO This provider should be registered and configured via a feature.
    @Inject
    public SaxParserFactoryInjectionProvider(final Configuration config) {
        super(config);
    }

    @Inject
    private InjectionManager injectionManager;

    @Override
    public SAXParserFactory get() {
        final SecureSaxParserFactory factory
                = new SecureSaxParserFactory(SAXParserFactory.newInstance(), !isXmlSecurityDisabled());

        factory.setNamespaceAware(true);

        final Map<String, Object> saxParserProperties = new LinkedHashMap<>();
        JaxbFeatureUtil.setProperties(injectionManager, SAXParser.class, saxParserProperties::put);
        factory.setSaxParserProperties(saxParserProperties);

        JaxbFeatureUtil.setFeatures(injectionManager, SAXParserFactory.class, factory::setFeature);

        return factory;
    }
}
