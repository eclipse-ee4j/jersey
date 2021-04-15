/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

public interface ParentContainerRequestFilter extends ParentChecker, ContainerRequestFilter {

    @Override
    default void filter(ContainerRequestContext requestContext) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean injected = false;

        if (requestContext.getUriInfo().getRequestUri().toASCIIString().contains("injected")) {
            injected = checkInjected(stringBuilder);
        }

        if (requestContext.getUriInfo().getRequestUri().toASCIIString().contains("contexted")) {
            injected = checkContexted(stringBuilder);
        }

        if (!injected) {
            requestContext.setProperty(ParentWriterInterceptor.STATUS, Response.Status.EXPECTATION_FAILED);
            stringBuilder.insert(0, "InjectContainerRequestFilter: ");
            requestContext.abortWith(
                    Response.status(Response.Status.EXPECTATION_FAILED).entity(stringBuilder.toString()).build()
            );
        }
    }
}
