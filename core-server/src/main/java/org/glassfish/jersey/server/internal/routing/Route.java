/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;

import org.glassfish.jersey.uri.PathPattern;

/**
 * Request routing information. Contains a {@link #routingPattern() routing pattern}
 * and a {@link #next() list of next-level stages} to be processed in case the
 * routing pattern successfully matches the un-matched right-hand part of the request.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class Route {
    private final PathPattern routingPattern;
    private final List<Router> routers;

    /**
     * Create a new request route.
     *
     * @param routingPattern request path routing pattern.
     * @param routers        next-level routers to be processed in case the routing
     *                       pattern matches the unmatched right-hand part of the request path.
     * @return new request route.
     */
    static Route of(PathPattern routingPattern, List<Router> routers) {
        return new Route(routingPattern, routers);
    }

    private Route(PathPattern routingPattern, List<Router> routers) {
        this.routingPattern = routingPattern;
        // MUST NOT try to substitute for Collections.emptyList() is the routers list is empty as it can be filled in later.
        // See PathMatchingRouterBuilder.startNewRoute(...) method.
        this.routers = routers;
    }

    /**
     * Get the request path routing pattern.
     *
     * @return request path routing pattern.
     */
    public PathPattern routingPattern() {
        return routingPattern;
    }

    /**
     * Get next-level routers to be processed in case the routing pattern matches
     * the unmatched right-hand part of the request path.
     *
     * @return routed next-level next.
     */
    public List<Router> next() {
        return routers;
    }
}
