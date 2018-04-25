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

import org.glassfish.jersey.server.internal.process.Endpoint;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;

/**
 * Routing tree assembly utilities.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class Routers {

    private static final Router IDENTITY_ROUTER = new Router() {

        @Override
        public Continuation apply(final RequestProcessingContext data) {
            return Continuation.of(data);
        }
    };

    private Routers() {
        throw new AssertionError("No instances of this class.");
    }

    /**
     * Create a terminal "no-op router" that accepts any input context and returns the unchanged request and an empty
     * continuation iterator.
     *
     * @return a terminal "no-op" router.
     */
    public static Router noop() {
        return IDENTITY_ROUTER;
    }

    /**
     * Creates a terminal {@link Router} that wraps the given {@link org.glassfish.jersey.server.internal.process.Endpoint
     * endpoint}.
     *
     * The {@link Router#apply} method of the created hierarchical router returns the unchanged request and an empty
     * continuation iterator.
     *
     * @param endpoint a server-side endpoint to be wrapped in a router instance.
     * @return a router that wraps the supplied endpoint.
     */
    public static Router endpoint(final Endpoint endpoint) {
        return new EndpointRouter(endpoint);
    }

    /**
     * Extract endpoint stored in a router (if any).
     *
     * @param router router from which a server endpoint should be extracted.
     * @return extracted endpoint or {@code null} if there was no endpoint stored in the router.
     */
    public static Endpoint extractEndpoint(final Router router) {
        if (router instanceof EndpointRouter) {
            return ((EndpointRouter) router).endpoint;
        }

        return null;
    }

    private static class EndpointRouter implements Router {

        private final Endpoint endpoint;

        public EndpointRouter(final Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public Continuation apply(RequestProcessingContext context) {
            return Continuation.of(context);
        }
    }
}
