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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.DataStructures;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * Server-side implementation of {@link org.glassfish.jersey.message.filtering.spi.ScopeProvider scope provider}. In addition to
 * {@link CommonScopeProvider base implementation} this class provides entity-filtering scopes by examining matched resource
 * method and sub-resource locators. This examination comes into play only in case if entity-filtering scopes cannot be found in
 * entity annotations or application configuration.
 *
 * @author Michal Gajdos
 */
@Singleton
@Priority(Priorities.ENTITY_CODER + 200)
@ConstrainedTo(RuntimeType.SERVER)
class ServerScopeProvider extends CommonScopeProvider {

    @Inject
    private Provider<ExtendedUriInfo> uriInfoProvider;

    private final ConcurrentMap<String, Set<String>> uriToContexts;

    /**
     * Create new server scope provider with injected {@link Configuration configuration} and
     * {@link InjectionManager jersey injection manager}.
     */
    @Inject
    public ServerScopeProvider(final Configuration config, final InjectionManager injectionManager) {
        super(config, injectionManager);
        this.uriToContexts = DataStructures.createConcurrentMap();
    }

    @Override
    public Set<String> getFilteringScopes(final Annotation[] entityAnnotations, final boolean defaultIfNotFound) {
        Set<String> filteringScope = super.getFilteringScopes(entityAnnotations, false);

        if (filteringScope.isEmpty()) {
            final ExtendedUriInfo uriInfo = uriInfoProvider.get();
            final String path = uriInfo.getPath();

            if (uriToContexts.containsKey(path)) {
                return uriToContexts.get(path);
            }

            for (final ResourceMethod method : ServerScopeProvider.getMatchedMethods(uriInfo)) {
                final Invocable invocable = method.getInvocable();

                mergeFilteringScopes(filteringScope,
                        getFilteringScopes(invocable.getHandlingMethod(), invocable.getHandler().getHandlerClass()));

                if (!filteringScope.isEmpty()) {
                    uriToContexts.putIfAbsent(path, filteringScope);
                    return filteringScope;
                }
            }
        }

        // Use default scope if not in other scope.
        return returnFilteringScopes(filteringScope, defaultIfNotFound);
    }

    /**
     * Get entity-filtering scopes from examining annotations present on resource method and resource class.
     *
     * @param resourceMethod matched resource method to be examined.
     * @param resourceClass matched resource class to be examined.
     * @return entity-filtering scopes or an empty set if the scopes cannot be obtained.
     */
    protected Set<String> getFilteringScopes(final Method resourceMethod, final Class<?> resourceClass) {
        // Method annotations first.
        Set<String> scope = getFilteringScopes(resourceMethod.getAnnotations());

        // Class annotations second.
        if (scope.isEmpty()) {
            scope = getFilteringScopes(resourceClass.getAnnotations());
        }

        return scope;
    }

    private static List<ResourceMethod> getMatchedMethods(final ExtendedUriInfo uriInfo) {
        final List<ResourceMethod> matchedResourceLocators = uriInfo.getMatchedResourceLocators();
        final List<ResourceMethod> methods = new ArrayList<>(1 + matchedResourceLocators.size());

        methods.add(uriInfo.getMatchedResourceMethod());
        methods.addAll(matchedResourceLocators);

        return methods;
    }
}
