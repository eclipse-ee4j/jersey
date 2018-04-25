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

package org.glassfish.jersey.jettison;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jettison.internal.entity.JettisonArrayProvider;
import org.glassfish.jersey.jettison.internal.entity.JettisonJaxbElementProvider;
import org.glassfish.jersey.jettison.internal.entity.JettisonListElementProvider;
import org.glassfish.jersey.jettison.internal.entity.JettisonObjectProvider;
import org.glassfish.jersey.jettison.internal.entity.JettisonRootElementProvider;

/**
 * Feature used to register Jettison JSON providers.
 *
 * @author Michal Gajdos
 */
public class JettisonFeature implements Feature {

    private static final String JSON_FEATURE = JettisonFeature.class.getSimpleName();

    private static Class[] PROVIDERS = new Class[] {
            JettisonArrayProvider.App.class,
            JettisonArrayProvider.General.class,

            JettisonObjectProvider.App.class,
            JettisonObjectProvider.General.class,

            JettisonRootElementProvider.App.class,
            JettisonRootElementProvider.General.class,

            JettisonJaxbElementProvider.App.class,
            JettisonJaxbElementProvider.General.class,

            JettisonListElementProvider.App.class,
            JettisonListElementProvider.General.class
    };

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        final String jsonFeature = CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);
        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        for (final Class<?> provider : PROVIDERS) {
            context.register(provider, MessageBodyReader.class, MessageBodyWriter.class);
        }

        return true;
    }
}
