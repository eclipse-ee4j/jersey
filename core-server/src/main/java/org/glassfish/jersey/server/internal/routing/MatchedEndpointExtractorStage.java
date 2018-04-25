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

import org.glassfish.jersey.process.internal.Stage;
import org.glassfish.jersey.process.internal.Stages;
import org.glassfish.jersey.server.internal.process.Endpoint;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;

/**
 * Request pre-processing stage that {@link RoutingContext#getEndpoint() extracts
 * an inflector from a routing context} where it was previously stored by the
 * {@link RoutingStage request to resource matching stage} and
 * (if available) returns the inflector wrapped in a next terminal stage.
 *
 * This request pre-processing stage should be a final stage in the request
 * processing chain.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see RoutingStage
 */
final class MatchedEndpointExtractorStage implements Stage<RequestProcessingContext> {

    @Override
    public Continuation<RequestProcessingContext> apply(final RequestProcessingContext processingContext) {
        final Endpoint endpoint =
                processingContext.routingContext().getEndpoint();

        return endpoint != null
                ? Continuation.of(processingContext, Stages.asStage(endpoint))
                : Continuation.of(processingContext);
    }
}
