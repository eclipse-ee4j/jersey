/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.Configuration;

import javax.inject.Inject;
import javax.xml.stream.XMLInputFactory;

/**
 * Thread-scoped injection provider of {@link XMLInputFactory transformer factories}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public class XmlInputFactoryInjectionProvider extends AbstractXmlFactory<XMLInputFactory> {

    /**
     * Create new XML input factory provider.
     *
     * @param config Jersey configuration properties.
     */
    // TODO This provider should be registered and configured via a feature.
    @Inject
    public XmlInputFactoryInjectionProvider(final InjectionManager injectionManager, final Configuration config) {
        super(config);
        this.injectionManager = injectionManager;
    }

    private InjectionManager injectionManager;

    @Override
    public XMLInputFactory get() {
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        if (!isXmlSecurityDisabled()) {
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        }

        JaxbFeatureUtil.setProperties(injectionManager, XMLInputFactory.class, factory::setProperty);

        return factory;
    }
}
