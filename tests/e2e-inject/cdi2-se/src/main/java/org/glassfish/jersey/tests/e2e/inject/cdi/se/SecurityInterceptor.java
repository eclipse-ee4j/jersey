/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Interceptor checking James as a user in query params.
 *
 * @author Petr Bouda
 */
@Secured
@Interceptor
public class SecurityInterceptor {

    @Inject
    NameService nameService;

    @Inject
    JaxrsService jaxrsService;

    @AroundInvoke
    public Object logMethodEntry(InvocationContext ctx) throws Exception {
        MultivaluedMap<String, String> params = jaxrsService.getUriInfo().getQueryParameters();
        String user = params.getFirst("user");

        if (nameService.getName().equals(user)) {
            return ctx.proceed();
        } else {
            throw new ForbiddenException("Forbidden resource for the user: " + user);
        }
    }
}
