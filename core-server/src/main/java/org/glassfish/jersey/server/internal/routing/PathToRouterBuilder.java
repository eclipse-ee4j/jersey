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

/**
 * An intermediate path matching router builder.
 *
 * This builder completes a routing information for a single routed {@link org.glassfish.jersey.uri.PathPattern}.
 * In case the unmatched right-hand part of the request path is matched by the routed path pattern, the request
 * processing context will be serially routed to all the child routers attached to the routing pattern using this
 * routing completion builder.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@SuppressWarnings("ClassReferencesSubclass")
interface PathToRouterBuilder {

    /**
     * Register a new next-level router to be used for request routing in case the routing pattern matches the
     * unmatched right-hand part of the request path.
     *
     * @param router new next-level router to be registered with the routed path pattern.
     * @return updated route builder ready to build a new {@link Router router} instance
     * (or add more routes to the currently built one).
     */
    PathMatchingRouterBuilder to(Router router);
}
