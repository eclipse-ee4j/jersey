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

import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.server.model.MethodHandler;

/**
 * Terminal router that pushes the matched method's handler instance to the stack
 * returned by {@link javax.ws.rs.core.UriInfo#getMatchedResources()} method.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PushMethodHandlerRouter implements Router {

    private final MethodHandler methodHandler;
    private final Router next;

    /**
     * Create a new {@code PushMethodHandlerRouter} instance.
     *
     * @param methodHandler method handler model providing the method handler
     *                      instance.
     * @param next          next router to be invoked after the this one.
     */
    PushMethodHandlerRouter(final MethodHandler methodHandler, final Router next) {
        this.methodHandler = methodHandler;
        this.next = next;
    }

    @Override
    public Continuation apply(final RequestProcessingContext context) {
        final RoutingContext routingContext = context.routingContext();

        final Object storedResource = routingContext.peekMatchedResource();
        if (storedResource == null || !storedResource.getClass().equals(methodHandler.getHandlerClass())) {
            Object handlerInstance = methodHandler.getInstance(context.injectionManager());
            routingContext.pushMatchedResource(handlerInstance);
        }
        return Continuation.of(context, next);
    }
}
