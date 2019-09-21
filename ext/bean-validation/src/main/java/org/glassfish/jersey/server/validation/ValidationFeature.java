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

package org.glassfish.jersey.server.validation;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.internal.ValidationBinder;

/**
 * {@code ValidationFeature} used to add Bean Validation (JSR-349) support to the server.
 *
 * @author Michal Gajdos
 */
@ConstrainedTo(RuntimeType.SERVER)
public final class ValidationFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        // Validation disabled?
        if (PropertiesHelper.isProperty(config.getProperty(ServerProperties.BV_FEATURE_DISABLE))) {
            return false;
        }

        context.register(new ValidationBinder());

        // Set ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR to make sure no sendError is called on servlet container
        // when ServerProperties.BV_SEND_ERROR_IN_RESPONSE is enabled.
        if (PropertiesHelper.isProperty(config.getProperty(ServerProperties.BV_SEND_ERROR_IN_RESPONSE))
                && config.getProperty(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR) == null) {
            context.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        }

        return true;
    }
}
