/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Thread-scoped injection provider of {@link DocumentBuilderFactory document
 * builder factories}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public class DocumentBuilderFactoryInjectionProvider extends AbstractXmlFactory<DocumentBuilderFactory> {

    /**
     * Create new document builder factory provider.
     *
     * @param config Jersey configuration properties.
     */
    // TODO This provider should be registered and configured via a feature.
    @Inject
    public DocumentBuilderFactoryInjectionProvider(final Configuration config) {
        super(config);
    }

    @Override
    public DocumentBuilderFactory get() {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();

        f.setNamespaceAware(true);

        if (!isXmlSecurityDisabled()) {
            f.setExpandEntityReferences(false);
        }

        return f;
    }

}
