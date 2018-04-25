/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.logging;

import java.util.Map;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_NAME;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_NAME_CLIENT;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_LOGGER_NAME_SERVER;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_MAX_ENTITY_SIZE;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_VERBOSITY;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT;
import static org.glassfish.jersey.logging.LoggingFeature.LOGGING_FEATURE_VERBOSITY_SERVER;

/**
 * Auto-discoverable class that registers {@link LoggingFeature} based on configuration properties.
 * <p>
 * Feature is registered if any of the common properties (see {@link LoggingFeature}) is set or any of the client properties is
 * set and context's {@link RuntimeType} is {@link RuntimeType#CLIENT} or any of the server properties is set and context's
 * {@link RuntimeType} is {@link RuntimeType#SERVER}.
 * <p>
 * The registration does not occur if the feature is already registered or auto-discoverable mechanism is disabled.
 *
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
 * @since 2.23
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public final class LoggingFeatureAutoDiscoverable implements AutoDiscoverable {

    @Override
    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(LoggingFeature.class)) {

            Map properties = context.getConfiguration().getProperties();

            if (commonPropertyConfigured(properties)
                    || (context.getConfiguration().getRuntimeType() == RuntimeType.CLIENT && clientConfigured(properties))
                    || (context.getConfiguration().getRuntimeType() == RuntimeType.SERVER && serverConfigured(properties))) {
                context.register(LoggingFeature.class);
            }
        }
    }

    private boolean commonPropertyConfigured(Map properties) {
        return properties.containsKey(LOGGING_FEATURE_LOGGER_NAME)
                || properties.containsKey(LOGGING_FEATURE_LOGGER_LEVEL)
                || properties.containsKey(LOGGING_FEATURE_VERBOSITY)
                || properties.containsKey(LOGGING_FEATURE_MAX_ENTITY_SIZE);
    }

    private boolean clientConfigured(Map properties) {
        return properties.containsKey(LOGGING_FEATURE_LOGGER_NAME_CLIENT)
                || properties.containsKey(LOGGING_FEATURE_LOGGER_LEVEL_CLIENT)
                || properties.containsKey(LOGGING_FEATURE_VERBOSITY_CLIENT)
                || properties.containsKey(LOGGING_FEATURE_MAX_ENTITY_SIZE_CLIENT);
    }

    private boolean serverConfigured(Map properties) {
        return properties.containsKey(LOGGING_FEATURE_LOGGER_NAME_SERVER)
                || properties.containsKey(LOGGING_FEATURE_LOGGER_LEVEL_SERVER)
                || properties.containsKey(LOGGING_FEATURE_VERBOSITY_SERVER)
                || properties.containsKey(LOGGING_FEATURE_MAX_ENTITY_SIZE_SERVER);
    }
}
