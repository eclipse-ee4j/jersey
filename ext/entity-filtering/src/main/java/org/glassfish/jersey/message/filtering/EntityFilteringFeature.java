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

/**
 * {@link Feature} used to add support for Entity Data Filtering feature for entity-filtering annotations based on
 * {@link EntityFiltering} meta-annotation.
 *
 * @author Michal Gajdos
 */
public final class EntityFilteringFeature implements Feature {

    /**
     * Defines one or more annotations that should be used as entity-filtering scope when reading/writing an entity.
     * <p>
     * The property can be used on client to define the scope as well as on server to override the scope derived from current
     * request processing context (resource methods / resource classes).
     * </p>
     * <p>
     * If the property is set, the specified annotations will be used to create (override) entity-filtering scope.
     * </p>
     * <p>
     * The property value MUST be an instance of {@link java.lang.annotation.Annotation} or {@code Annotation[]} array. To obtain
     * the annotation instances refer to the {@link EntityFiltering} for requirements on creating entity-filtering annotations.
     * </p>
     * <p>
     * A default value is not set.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @see EntityFiltering
     */
    public static final String ENTITY_FILTERING_SCOPE = "jersey.config.entityFiltering.scope";

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(EntityFilteringProcessor.class)) {
            // Binder (FilteringObjectProvider/FilteringGraphTransformer).
            if (!config.isRegistered(EntityFilteringBinder.class)) {
                context.register(new EntityFilteringBinder());
            }

            // Entity Processors.
            context.register(EntityFilteringProcessor.class);
            if (!config.isRegistered(DefaultEntityProcessor.class)) {
                context.register(DefaultEntityProcessor.class);
            }

            // Scope Providers.
            context.register(EntityFilteringScopeResolver.class);

            // Scope Resolver.
            if (RuntimeType.SERVER == config.getRuntimeType()) {
                context.register(ServerScopeProvider.class);
            } else {
                context.register(CommonScopeProvider.class);
            }

            return true;
        }
        return false;
    }

    /**
     * Return {@code true} whether at least one of the entity filtering features is registered in the given config.
     *
     * @param config config to be examined for presence of entity filtering feature.
     * @return {@code true} if entity filtering is enabled for given config, {@code false} otherwise.
     */
    public static boolean enabled(final Configuration config) {
        return config.isRegistered(EntityFilteringFeature.class)
                || config.isRegistered(SecurityEntityFilteringFeature.class)
                || config.isRegistered(SelectableEntityFilteringFeature.class);
    }
}
