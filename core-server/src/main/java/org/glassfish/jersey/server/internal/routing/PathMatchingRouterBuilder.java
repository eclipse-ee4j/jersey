/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.routing;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.jersey.uri.PathPattern;

/**
 /**
 * A request path pattern matching router hierarchy builder entry point.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PathMatchingRouterBuilder implements PathToRouterBuilder {

    private final List<Route> acceptedRoutes = new LinkedList<>();
    private List<Router> currentRouters;


    /**
     * Create new request path pattern matching router builder.
     *
     * @param pattern request path matching pattern.
     * @return new request path pattern matching router builder.
     */
    static PathToRouterBuilder newRoute(final PathPattern pattern) {
        final PathMatchingRouterBuilder builder = new PathMatchingRouterBuilder();
        builder.startNewRoute(pattern);
        return builder;
    }

    private PathMatchingRouterBuilder() {
        // preventing direct instantiation
    }

    private void startNewRoute(final PathPattern pattern) {
        currentRouters = new LinkedList<>();
        acceptedRoutes.add(Route.of(pattern, currentRouters));
    }

    /**
     * Get the list of the registered sub-routes.
     *
     * @return list of the registered sub-routes.
     */
    protected List<Route> acceptedRoutes() {
        return acceptedRoutes;
    }

    @Override
    public PathMatchingRouterBuilder to(final Router router) {
        currentRouters.add(router);
        return this;
    }

    /**
     * Complete the currently built unfinished sub-route (if any) and start building a new one.
     *
     * The completed sub-route is added to the list of the routes accepted by the router that is being built.
     *
     * @param pattern routing pattern for the new sub-route.
     * @return updated router builder.
     */
    public PathToRouterBuilder route(final PathPattern pattern) {
        startNewRoute(pattern);
        return this;
    }

    /**
     * Build a {@link org.glassfish.jersey.server.internal.routing.Router hierarchical request path matching processor}.
     *
     * @return hierarchical request path matching processor (i.e. router).
     */
    public PathMatchingRouter build() {
        return new PathMatchingRouter(acceptedRoutes());
    }

}
