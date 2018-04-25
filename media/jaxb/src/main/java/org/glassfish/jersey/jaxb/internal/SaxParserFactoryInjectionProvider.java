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

package org.glassfish.jersey.jaxb.internal;

import javax.ws.rs.core.Configuration;

import javax.inject.Inject;
import javax.xml.parsers.SAXParserFactory;

/**
 * Thread-scoped injection provider of {@link SAXParserFactory SAX parser factories}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
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

    @Override
    public SAXParserFactory get() {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);

        if (!isXmlSecurityDisabled()) {
            factory = new SecureSaxParserFactory(factory);
        }

        return factory;
    }
}
