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

package org.glassfish.jersey.moxy.json;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.internal.MoxyFilteringFeature;
import org.glassfish.jersey.moxy.json.internal.ConfigurableMoxyJsonProvider;
import org.glassfish.jersey.moxy.json.internal.FilteringMoxyJsonProvider;

/**
 * Feature used to register MOXy JSON providers.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Michal Gajdos
 */
public class MoxyJsonFeature implements Feature {

    private static final String JSON_FEATURE = MoxyJsonFeature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                CommonProperties.MOXY_JSON_FEATURE_DISABLE, Boolean.FALSE, Boolean.class)) {
            return false;
        }

        final String jsonFeature = CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);
        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        // Set a slightly lower priority of workers than JSON-P so MOXy is not pick-ed up for JsonStructures (if both are used).
        final int workerPriority = Priorities.USER + 2000;

        if (EntityFilteringFeature.enabled(config)) {
            context.register(MoxyFilteringFeature.class);
            context.register(FilteringMoxyJsonProvider.class, workerPriority);
        } else {
            context.register(ConfigurableMoxyJsonProvider.class, workerPriority);
        }

        return true;
    }
}
