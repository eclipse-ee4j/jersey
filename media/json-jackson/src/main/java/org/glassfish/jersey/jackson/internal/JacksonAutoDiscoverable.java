/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * {@link AutoDiscoverable} registering {@link JacksonFeature} if the feature is not already registered.
 *
 * @author Michal Gajdos
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class JacksonAutoDiscoverable implements AutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(JacksonFeature.class)) {
            context.register(JacksonFeature.class);
        }
    }
}
