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

import org.glassfish.jersey.server.internal.process.RequestProcessingContext;

/**
 * Request matching bootstrapping stage that pushes the whole request path to the routing
 * context as a right-hand path to be matched.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class MatchResultInitializerRouter implements Router {

    private final Router rootRouter;

    /**
     * Create a new match result initializer.
     *
     * @param rootRouter root router.
     */
    MatchResultInitializerRouter(Router rootRouter) {
        this.rootRouter = rootRouter;
    }

    @Override
    public Continuation apply(final RequestProcessingContext processingContext) {
        final RoutingContext rc = processingContext.routingContext();
        rc.pushMatchResult(new SingleMatchResult("/" + processingContext.request().getPath(false)));

        return Continuation.of(processingContext, rootRouter);
    }
}
