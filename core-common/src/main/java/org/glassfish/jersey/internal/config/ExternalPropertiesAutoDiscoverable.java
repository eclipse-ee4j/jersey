/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.config;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

@ConstrainedTo(RuntimeType.CLIENT) //server is configured directly in ResourceConfig
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class ExternalPropertiesAutoDiscoverable implements AutoDiscoverable {
    @Override
    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(ExternalPropertiesConfigurationFeature.class)) {
            context.register(ExternalPropertiesConfigurationFeature.class);
        }
    }
}