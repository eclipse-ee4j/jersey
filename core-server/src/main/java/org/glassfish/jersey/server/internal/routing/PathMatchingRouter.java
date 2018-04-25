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

import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;

import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.internal.ServerTraceEvent;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.uri.PathPattern;

/**
 * Matches the un-matched right-hand request path to the configured collection of path pattern matching routes.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PathMatchingRouter implements Router {

    private final List<Route> acceptedRoutes;

    /**
     * Constructs route methodAcceptorPair that uses {@link PathPattern} instances for
     * patch matching.
     *
     * @param routes   next-level request routers to be returned in case the router matching
     *                 the built router is successful.
     */
    PathMatchingRouter(final List<Route> routes) {
        this.acceptedRoutes = routes;
    }

    @Override
    public Router.Continuation apply(final RequestProcessingContext context) {
        final RoutingContext rc = context.routingContext();
        // Peek at matching information to obtain path to match
        String path = rc.getFinalMatchingGroup();

        final TracingLogger tracingLogger = TracingLogger.getInstance(context.request());
        tracingLogger.log(ServerTraceEvent.MATCH_PATH_FIND, path);

        Router.Continuation result = null;
        final Iterator<Route> iterator = acceptedRoutes.iterator();
        while (iterator.hasNext()) {
            final Route acceptedRoute = iterator.next();
            final PathPattern routePattern = acceptedRoute.routingPattern();
            final MatchResult m = routePattern.match(path);
            if (m != null) {
                // Push match result information and rest of path to match
                rc.pushMatchResult(m);
                result = Router.Continuation.of(context, acceptedRoute.next());

                //tracing
                tracingLogger.log(ServerTraceEvent.MATCH_PATH_SELECTED, routePattern.getRegex());
                break;
            } else {
                tracingLogger.log(ServerTraceEvent.MATCH_PATH_NOT_MATCHED, routePattern.getRegex());
            }
        }

        if (tracingLogger.isLogEnabled(ServerTraceEvent.MATCH_PATH_SKIPPED)) {
            while (iterator.hasNext()) {
                tracingLogger.log(ServerTraceEvent.MATCH_PATH_SKIPPED, iterator.next().routingPattern().getRegex());
            }
        }

        if (result == null) {
            // No match
            return Router.Continuation.of(context);
        }

        return result;
    }
}
