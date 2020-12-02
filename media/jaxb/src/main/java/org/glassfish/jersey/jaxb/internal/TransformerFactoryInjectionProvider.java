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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Configuration;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Thread-scoped injection provider of {@link TransformerFactory transformer factories}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public class TransformerFactoryInjectionProvider extends AbstractXmlFactory<TransformerFactory> {

    private static final Logger LOGGER = Logger.getLogger(TransformerFactoryInjectionProvider.class.getName());

    /**
     * Create new transformer factory provider.
     *
     * @param config Jersey configuration properties.
     */
    // TODO This provider should be registered and configured via a feature.
    @Inject
    public TransformerFactoryInjectionProvider(final Configuration config) {
        super(config);
    }

    @Inject
    private InjectionManager injectionManager;

    @Override
    public TransformerFactory get() {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();

        if (!isXmlSecurityDisabled()) {
            try {
                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
                LOGGER.log(Level.CONFIG, LocalizationMessages.UNABLE_TO_SECURE_XML_TRANSFORMER_PROCESSING(), e);
            }
        }

        JaxbFeatureUtil.setFeatures(injectionManager, TransformerFactory.class, transformerFactory::setFeature);
        JaxbFeatureUtil.setProperties(injectionManager, TransformerFactory.class, transformerFactory::setAttribute);

        return transformerFactory;
    }
}
