/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.wadl;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import javax.inject.Singleton;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.wadl.internal.WadlApplicationContextImpl;
import org.glassfish.jersey.server.wadl.internal.generators.WadlGeneratorJAXBGrammarGenerator;
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;

import java.util.logging.Logger;


/**
 * Feature enabling WADL processing. The feature registers providers and binders needed to enable wadl in the
 * jersey application. One of the providers is {@link ModelProcessor wadl model processor} which enhances
 * current resources by additional wadl resources like {@code /application.wadl} and wadl options method.
 *
 * @author Miroslav Fuksa
 */
public class WadlFeature implements Feature {

    private static final Logger LOGGER = Logger.getLogger(WadlFeature.class.getName());

    @Override
    public boolean configure(FeatureContext context) {
        final boolean disabled = PropertiesHelper.isProperty(context.getConfiguration().getProperty(ServerProperties
                .WADL_FEATURE_DISABLE));
        if (disabled) {
            return false;
        }

        if (!isJaxB()) {
            LOGGER.warning(LocalizationMessages.WADL_FEATURE_DISABLED());
            return false;
        }

        context.register(WadlModelProcessor.class);
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(WadlApplicationContextImpl.class).to(WadlApplicationContext.class).in(Singleton.class);
            }
        });

        return true;
    }

    private static boolean isJaxB() {
        try {
            return null != WadlApplicationContextImpl.getJAXBContextFromWadlGenerator(new WadlGeneratorJAXBGrammarGenerator());
        } catch (JAXBException je) {
            return false;
        }
    }
}
