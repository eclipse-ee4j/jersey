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

/**
 * Terminal router that pushes the URI matched so far to the stack returned
 * by {@link javax.ws.rs.core.UriInfo#getMatchedURIs()} method.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PushMatchedUriRouter implements Router {

    @Override
    public Continuation apply(final RequestProcessingContext context) {
        context.routingContext().pushLeftHandPath();

        return Continuation.of(context);
    }
}
