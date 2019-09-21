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
import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * Router that pushes matched {@link ResourceMethod resource method}
 * to {@link RoutingContext routing context}.
 *
 * @author Miroslav Fuksa
 */
final class PushMatchedMethodRouter implements Router {

    private final ResourceMethod resourceMethod;

    /**
     * Create a new instance of push matched resource router.
     *
     * @param resourceMethod The matched resource method that should be pushed into the
     *                       {@link RoutingContext routing context}.
     */
    PushMatchedMethodRouter(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    @Override
    public Continuation apply(final RequestProcessingContext processingContext) {

        final RoutingContext rc = processingContext.routingContext();

        switch (resourceMethod.getType()) {
            case RESOURCE_METHOD:
            case SUB_RESOURCE_METHOD:
                rc.setMatchedResourceMethod(resourceMethod);
                break;
            case SUB_RESOURCE_LOCATOR:
                rc.pushMatchedLocator(resourceMethod);
                break;
        }

        return Continuation.of(processingContext);
    }
}
