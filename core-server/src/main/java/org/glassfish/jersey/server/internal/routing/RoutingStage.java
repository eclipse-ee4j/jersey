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

import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.process.internal.AbstractChainableStage;
import org.glassfish.jersey.process.internal.Stage;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.ServerTraceEvent;
import org.glassfish.jersey.server.internal.process.Endpoint;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Request pre-processing stage that encapsulates hierarchical resource matching
 * and request routing.
 *
 * Once the routing is finished, an endpoint (if matched) is
 * {@link RoutingContext#setEndpoint stored in the routing context}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see MatchedEndpointExtractorStage
 */
final class RoutingStage extends AbstractChainableStage<RequestProcessingContext> {

    private final Router routingRoot;

    /**
     * Create a new routing stage instance.
     *
     * @param routingRoot root router.
     */
     RoutingStage(final Router routingRoot) {
        this.routingRoot = routingRoot;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Routing stage navigates through the nested {@link Router routing hierarchy}
     * using a depth-first transformation strategy until a request-to-response
     * inflector is {@link org.glassfish.jersey.process.internal.Inflecting found on
     * a leaf stage node}, in which case the request routing is terminated and an
     * {@link org.glassfish.jersey.process.Inflector inflector} (if found) is pushed
     * to the {@link RoutingContext routing context}.
     */
    @Override
    public Continuation<RequestProcessingContext> apply(final RequestProcessingContext context) {
        final ContainerRequest request = context.request();
        context.triggerEvent(RequestEvent.Type.MATCHING_START);

        final TracingLogger tracingLogger = TracingLogger.getInstance(request);
        final long timestamp = tracingLogger.timestamp(ServerTraceEvent.MATCH_SUMMARY);
        try {
            final RoutingResult result = _apply(context, routingRoot);

            Stage<RequestProcessingContext> nextStage = null;
            if (result.endpoint != null) {
                context.routingContext().setEndpoint(result.endpoint);
                nextStage = getDefaultNext();
            }

            return Continuation.of(result.context, nextStage);
        } finally {
            tracingLogger.logDuration(ServerTraceEvent.MATCH_SUMMARY, timestamp);
        }
    }

    @SuppressWarnings("unchecked")
    private RoutingResult _apply(final RequestProcessingContext request, final Router router) {

        final Router.Continuation continuation = router.apply(request);

        for (Router child : continuation.next()) {
            RoutingResult result = _apply(continuation.requestContext(), child);

            if (result.endpoint != null) {
                // we're done
                return result;
            } // else continue
        }

        Endpoint endpoint = Routers.extractEndpoint(router);
        if (endpoint != null) {
            // inflector at terminal stage found
            return RoutingResult.from(continuation.requestContext(), endpoint);
        }

        // inflector at terminal stage not found
        return RoutingResult.from(continuation.requestContext());
    }

    private static final class RoutingResult {
        private final RequestProcessingContext context;
        private final Endpoint endpoint;

        private static RoutingResult from(final RequestProcessingContext requestProcessingContext, final Endpoint endpoint) {
            return new RoutingResult(requestProcessingContext, endpoint);
        }

        private static RoutingResult from(final RequestProcessingContext requestProcessingContext) {
            return new RoutingResult(requestProcessingContext, null);
        }

        private RoutingResult(final RequestProcessingContext context, final Endpoint endpoint) {
            this.context = context;
            this.endpoint = endpoint;
        }
    }
}
