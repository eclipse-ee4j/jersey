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

import java.util.Arrays;
import java.util.List;

import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * A combination of a resource method model and the corresponding routers.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class MethodRouting {

    /**
     * Resource method model.
     */
    final ResourceMethod method;

    /**
     * Resource method routers.
     */
    final List<Router> routers;

    /**
     * Create a new instance.
     *
     * @param method  Resource method handler.
     * @param routers Routers that are needed to execute the {@code model}. These routers should contain a
     *                {@link Routers#endpoint(org.glassfish.jersey.server.internal.process.Endpoint) endpoint router}
     *                as the (last) terminal router.
     */
    MethodRouting(final ResourceMethod method, final Router... routers) {
        this.method = method;
        this.routers = Arrays.asList(routers);
    }

    @Override
    public String toString() {
        return "{" + method + " -> " + routers + '}';
    }
}
