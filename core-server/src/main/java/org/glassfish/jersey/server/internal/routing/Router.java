/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;

import org.glassfish.jersey.server.internal.process.RequestProcessingContext;

/**
 * Hierarchical request router that can be used to create dynamic routing tree
 * structures.  Each routing tree can be executed using a dedicated
 * {@link RoutingStage routing stage}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
interface Router {
    /**
     * Hierarchical request routing continuation.
     * <p>
     * A continuation of a hierarchical request routing is represented
     * by an ordered collection of next level of routers resulting
     * in a hierarchical depth-first (depth-only) request routing.
     * </p>
     */
    public static final class Continuation {
        private final RequestProcessingContext requestProcessingContext;
        private final Iterable<Router> next;

        /**
         * Create a terminal continuation from the routed request.
         *
         * @param result routed request.
         * @return terminal continuation with no {@link #next() next level routers}
         *         in the routing hierarchy and the supplied routed request.
         */
        static Continuation of(final RequestProcessingContext result) {
            return new Continuation(result, null);
        }

        /**
         * Create a continuation from the routed request and a collection
         * of next level routers.
         *
         * @param result routed request.
         * @param next   next level routers.
         * @return a continuation with the supplied next level routers to be invoked
         *         {@link #next() next} in the routing chain and the supplied routed
         *         request.
         */
        static Continuation of(final RequestProcessingContext result, Iterable<Router> next) {
            return new Continuation(result, next);
        }

        /**
         * Create a continuation from the routed request and a single
         * of next level routers.
         *
         * @param request routed request.
         * @param next    next level router.
         * @return a continuation with the supplied next level router to be invoked
         *         {@link #next() next} in the routing chain and the supplied routed
         *         request.
         */
        static Continuation of(final RequestProcessingContext request, final Router next) {
            return new Continuation(request, Collections.singletonList(next));
        }

        private Continuation(final RequestProcessingContext request, final Iterable<Router> next) {
            this.requestProcessingContext = request;
            this.next = (next == null) ? Collections.<Router>emptyList() : next;
        }

        /**
         * Get the routed request context.
         *
         * @return routed request context.
         */
        RequestProcessingContext requestContext() {
            return requestProcessingContext;
        }

        /**
         * Get the next level routers to be invoked or {@code an empty} if no next
         * level routers are present.
         *
         * @return the next level routers to be invoked or an empty collection if not
         *         present.
         */
        Iterable<Router> next() {
            return next;
        }
    }

    /**
     * Performs a request routing task and returns the routed request together with
     * a {@link Continuation routing continuation}.
     *
     * @param data data to be transformed.
     * @return a processing continuation.
     */
    public Continuation apply(RequestProcessingContext data);
}
