/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.server.model.RuntimeResource;

/**
 * Router that pushes {@link RuntimeResource runtime resource} matched during a any routing phase
 * to {@link org.glassfish.jersey.server.internal.routing.RoutingContext routing context}.
 *
 * @author Miroslav Fuksa
 */

final class PushMatchedRuntimeResourceRouter implements Router {

    private final RuntimeResource resource;

    /**
     * Create a new instance of push matched resource router.
     *
     * @param resource RuntimeResource runtime to be pushed into the {@link RoutingContext routing context}.
     */
    PushMatchedRuntimeResourceRouter(RuntimeResource resource) {
        this.resource = resource;
    }

    @Override
    public Continuation apply(final RequestProcessingContext context) {
        context.routingContext().pushMatchedRuntimeResource(resource);
        return Continuation.of(context);
    }
}
