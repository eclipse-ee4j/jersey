/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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
 * @author Marek Potociar
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
        MatchResult matchResultCandidate = null;
        Route acceptedRouteCandidate = null;

        final Iterator<Route> iterator = acceptedRoutes.iterator();
        while (iterator.hasNext()) {
            final Route acceptedRoute = iterator.next();
            final PathPattern routePattern = acceptedRoute.routingPattern();
            final MatchResult matchResult = routePattern.match(path);
            if (matchResult != null) {
                if (isLocator(acceptedRoute) && matchResultCandidate != null) {
                    // acceptedRoute matches the path but it is a locator
                    // sub-resource locator shall not be found by the Spec if sub-resource was found first
                    // need to use the sub-resource to return correct HTTP Status
                    // TODO configuration option not to fail and continue with acceptedRoute locator?
                    result = matchPathSelected(context, acceptedRouteCandidate, matchResultCandidate, tracingLogger);
                    break;
                } else if (isLocator(acceptedRoute) || designatorMatch(acceptedRoute, context)) {
                    result = matchPathSelected(context, acceptedRoute, matchResult, tracingLogger);
                    break;
                } else if (matchResultCandidate == null) {
                    // store the first matched candidate with unmatched method designator
                    // maybe there won't be a better sub-resource
                    matchResultCandidate = matchResult;
                    acceptedRouteCandidate = acceptedRoute;
                }
            } else {
                tracingLogger.log(ServerTraceEvent.MATCH_PATH_NOT_MATCHED, routePattern.getRegex());
            }
        }

        if (tracingLogger.isLogEnabled(ServerTraceEvent.MATCH_PATH_SKIPPED)) {
            while (iterator.hasNext()) {
                tracingLogger.log(ServerTraceEvent.MATCH_PATH_SKIPPED, iterator.next().routingPattern().getRegex());
            }
        }

        if (result == null && acceptedRouteCandidate != null) {
            //method designator mismatched, but still go the route to get the proper status code
            result = matchPathSelected(context, acceptedRouteCandidate, matchResultCandidate, tracingLogger);
        }

        if (result == null) {
            // No match
            return Router.Continuation.of(context);
        }

        return result;
    }

    private Router.Continuation matchPathSelected(final RequestProcessingContext context, final Route acceptedRoute,
                                                  final MatchResult matchResult, final TracingLogger tracingLogger) {
        // Push match result information and rest of path to match
        context.routingContext().pushMatchResult(matchResult);
        final Router.Continuation result = Router.Continuation.of(context, acceptedRoute.next());

        // tracing
        tracingLogger.log(ServerTraceEvent.MATCH_PATH_SELECTED, acceptedRoute.routingPattern().getRegex());

        return result;
    }

    /**
     * Return {@code true} iff the sub-resource method designator does match the request http method designator
     * @param route current route representing resource method / locator
     * @param context Contains Request to check the http method
     * @return false if method designator does not match
     */
    private static boolean designatorMatch(final Route route, final RequestProcessingContext context) {
        final String httpMethod = context.request().getMethod();

        if (route.getHttpMethods().contains(httpMethod)) {
            return true;
        }
        return ("HEAD".equals(httpMethod) && route.getHttpMethods().contains("GET"));
    }

    private static boolean isLocator(final Route route) {
        return route.getHttpMethods() == null;
    }
}
