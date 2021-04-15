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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ParentExceptionMapper extends RequestScopedParentInject implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        StringBuilder sb = new StringBuilder();
        boolean injected = true;
        switch (exception.getMessage()) {
            case "inject":
                injected = checkInjected(sb);
                break;
            default:
                injected = checkContexted(sb);
                break;
        }

        if (!injected) {
            contextContainerRequestContext.setProperty(ParentWriterInterceptor.STATUS, Response.Status.EXPECTATION_FAILED);
            sb.insert(0, exception.getMessage() + "ExceptionMapper: ");
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(sb.toString()).build();
        }
        return Response.ok("All " + exception.getMessage() + "ed on ExceptionMapper.").build();
    }
}
