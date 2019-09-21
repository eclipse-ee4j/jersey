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

package org.glassfish.jersey.message.filtering;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * {@link Feature} used to add support for custom query parameter filtering for
 * Entity Data Filtering feature. </p> Note: This feature also registers the
 * {@link EntityFilteringFeature}.
 *
 * @author Andy Pemberton (pembertona at gmail.com)
 * @see org.glassfish.jersey.message.filtering.EntityFilteringFeature
 */
public final class SelectableEntityFilteringFeature implements Feature {

    public static final String QUERY_PARAM_NAME = "jersey.config.entityFiltering.selectable.query";

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(SelectableEntityProcessor.class)) {

            // register EntityFilteringFeature
            if (!config.isRegistered(EntityFilteringFeature.class)) {
                context.register(EntityFilteringFeature.class);
            }
            // Entity Processors.
            context.register(SelectableEntityProcessor.class);
            // Scope Resolver.
            context.register(SelectableScopeResolver.class);

            return true;
        }
        return true;
    }
}
