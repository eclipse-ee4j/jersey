/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gson.internal;

import javax.annotation.Priority;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.gson.JsonGsonFeature;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

/**
 * {@link ForcedAutoDiscoverable} registering {@link JsonGsonFeature} if the feature is not already registered.
 * <p>
 *
 * @see JsonGsonFeature
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class JsonGsonAutoDiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(JsonGsonFeature.class)) {
            context.register(JsonGsonFeature.class);
        }
    }
}
