/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.filter.internal;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.UriConnegFilter;

/**
 * Enable auto-discoverable of certain server filters.
 *
 * @author Michal Gajdos
 */
@ConstrainedTo(RuntimeType.SERVER)
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public final class ServerFiltersAutoDiscoverable implements AutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        // UriConnegFilter.
        final Object languageMappings = config.getProperty(ServerProperties.LANGUAGE_MAPPINGS);
        final Object mediaTypesMappings = config.getProperty(ServerProperties.MEDIA_TYPE_MAPPINGS);

        if (!config.isRegistered(UriConnegFilter.class)
                && (languageMappings != null || mediaTypesMappings != null)) {
            context.register(UriConnegFilter.class);
        }
    }
}
