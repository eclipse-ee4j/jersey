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

package org.glassfish.jersey.message.filtering;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * {@link Feature} used to add support for Java Security annotations (<code>javax.annotation.security</code>) for Entity Data
 * Filtering feature.
 * <p>
 * Supported annotations are:
 * <ul>
 * <li>{@link javax.annotation.security.PermitAll}</li>
 * <li>{@link javax.annotation.security.RolesAllowed}</li>
 * <li>{@link javax.annotation.security.DenyAll}</li>
 * </ul>
 * </p>
 * <p>
 * It is sufficient to annotate only property accessors of an entity without annotating resource method / resource class although
 * it is not recommended.
 * </p>
 * Note: This feature also registers the {@link EntityFilteringFeature}.
 *
 * @author Michal Gajdos
 * @see org.glassfish.jersey.message.filtering.EntityFilteringFeature
 */
public final class SecurityEntityFilteringFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(SecurityEntityProcessor.class)) {
            // RolesAllowed feature.
            if (!config.isRegistered(RolesAllowedDynamicFeature.class)) {
                context.register(RolesAllowedDynamicFeature.class);
            }

            // Binder (FilteringObjectProvider/FilteringGraphTransformer).
            if (!config.isRegistered(EntityFilteringBinder.class)) {
                context.register(new EntityFilteringBinder());
            }

            // Entity Processors.
            context.register(SecurityEntityProcessor.class);
            if (!config.isRegistered(DefaultEntityProcessor.class)) {
                context.register(DefaultEntityProcessor.class);
            }

            // Scope Providers.
            context.register(SecurityScopeResolver.class);
            if (RuntimeType.SERVER.equals(config.getRuntimeType())) {
                context.register(SecurityServerScopeResolver.class);
            }

            // Scope Resolver.
            if (RuntimeType.SERVER == config.getRuntimeType()) {
                context.register(SecurityServerScopeProvider.class);
            } else {
                context.register(CommonScopeProvider.class);
            }

            return true;
        }
        return false;
    }
}
