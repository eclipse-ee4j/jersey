/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jsonb.internal;

import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.jsonb.JsonBindingFeature;

/**
 * {@link ForcedAutoDiscoverable} registering {@link JsonBindingFeature} if the feature is not already registered.
 * <p>
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @see JsonBindingFeature
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY - 200)
public class JsonBindingAutoDiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(JsonBindingFeature.class)) {
            context.register(JsonBindingFeature.class);
        }
    }
}
