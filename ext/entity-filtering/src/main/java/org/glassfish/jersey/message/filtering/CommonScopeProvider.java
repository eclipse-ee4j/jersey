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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.message.filtering.internal.LocalizationMessages;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;
import org.glassfish.jersey.message.filtering.spi.ScopeProvider;
import org.glassfish.jersey.message.filtering.spi.ScopeResolver;
import org.glassfish.jersey.model.internal.RankedComparator;

/**
 * Default implementation of {@link ScopeProvider scope provider}. This class can be used on client to retrieve
 * entity-filtering scopes from given entity annotations or injected {@link Configuration configuration}. Class can also serve
 * as a base class for server-side implementations.
 *
 * @author Michal Gajdos
 * @see ServerScopeProvider
 */
@Singleton
class CommonScopeProvider implements ScopeProvider {

    private static final Logger LOGGER = Logger.getLogger(CommonScopeProvider.class.getName());

    private final List<ScopeResolver> resolvers;
    private final Configuration config;

    /**
     * Create new common scope provider with injected {@link Configuration configuration} and
     * {@link InjectionManager injection manager}.
     */
    @Inject
    public CommonScopeProvider(final Configuration config, final InjectionManager injectionManager) {
        this.config = config;
        Spliterator<ScopeResolver> resolverSpliterator =
                Providers.getAllProviders(injectionManager, ScopeResolver.class, new RankedComparator<>()).spliterator();
        this.resolvers = StreamSupport.stream(resolverSpliterator, false).collect(Collectors.toList());
    }

    @Override
    public Set<String> getFilteringScopes(final Annotation[] entityAnnotations, final boolean defaultIfNotFound) {
        Set<String> filteringScopes = new HashSet<>();

        // Entity Annotations.
        filteringScopes.addAll(getFilteringScopes(entityAnnotations));

        if (filteringScopes.isEmpty()) {
            // Configuration.
            filteringScopes.addAll(getFilteringScopes(config));
        }

        // Use default scope if not in other scope.
        return returnFilteringScopes(filteringScopes, defaultIfNotFound);
    }

    /**
     * Return the default entity-filtering scope if the given set of scopes is empty and the processing should fallback to the
     * default.
     *
     * @param filteringScopes       entity-filtering scopes to be examined.
     * @param returnDefaultFallback {@code true} if the default entity-filtering scope should be returned if the given scopes
     *                              are empty, {@code false} otherwise.
     * @return entity-filtering scopes.
     */
    protected Set<String> returnFilteringScopes(final Set<String> filteringScopes, final boolean returnDefaultFallback) {
        return returnDefaultFallback && filteringScopes.isEmpty() ? FilteringHelper.getDefaultFilteringScope() : filteringScopes;
    }

    /**
     * Get entity-filtering scopes from all available {@link ScopeResolver scope resolvers} for given annotations.
     *
     * @param annotations annotations to retrieve entity-filtering scopes from.
     * @return entity-filtering scopes or an empty set if none scope can be resolved.
     */
    protected Set<String> getFilteringScopes(final Annotation[] annotations) {
        Set<String> filteringScopes = new HashSet<>();
        for (final ScopeResolver provider : resolvers) {
            mergeFilteringScopes(filteringScopes, provider.resolve(annotations));
        }
        return filteringScopes;
    }

    /**
     * Get entity-filtering scopes from {@link Configuration}.
     *
     * @param config configuration the entity-filtering scopes are obtained from.
     * @return entity-filtering scopes or an empty set if none scope can be resolved.
     */
    private Set<String> getFilteringScopes(final Configuration config) {
        final Object property = config.getProperty(EntityFilteringFeature.ENTITY_FILTERING_SCOPE);

        Set<String> filteringScopes = Collections.emptySet();
        if (property != null) {
            if (property instanceof Annotation) {
                filteringScopes = getFilteringScopes(new Annotation[] {(Annotation) property});
            } else if (property instanceof Annotation[]) {
                filteringScopes = getFilteringScopes((Annotation[]) property);
            } else {
                LOGGER.log(Level.CONFIG, LocalizationMessages.ENTITY_FILTERING_SCOPE_NOT_ANNOTATIONS(property));
            }
        }
        return filteringScopes;
    }

    /**
     * Merge two sets of entity-filtering scopes.
     *
     * @param filteringScopes existing entity-filtering scopes.
     * @param resolvedScopes entity-filtering scopes to be added to the existing ones.
     */
    protected void mergeFilteringScopes(final Set<String> filteringScopes, final Set<String> resolvedScopes) {
        if (!filteringScopes.isEmpty() && !resolvedScopes.isEmpty()) {
            LOGGER.log(Level.FINE, LocalizationMessages.MERGING_FILTERING_SCOPES());
        }

        filteringScopes.addAll(resolvedScopes);
    }
}
