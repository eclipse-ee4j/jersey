/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

public class ServletInject {
    @Inject
    @Any
    protected HttpServletRequest injectHttpServletRequest;

    @Inject
    @Any
    protected HttpServletResponse injectHttpServletResponse;

    @Inject
    @Any
    protected ServletConfig injectServletConfig;

    @Inject
    @Any
    protected ServletContext injectServletContext;

    public boolean check(StringBuilder sb) {
        boolean injected = true;
        injected &= InjectionChecker.checkHttpServletRequest(injectHttpServletRequest, sb);
        injected &= InjectionChecker.checkHttpServletResponse(injectHttpServletResponse, sb);
        injected &= InjectionChecker.checkServletConfig(injectServletConfig, sb);
        injected &= InjectionChecker.checkServletContext(injectServletContext, sb);
        return injected;
    }

    public Response check() {
        StringBuilder sb = new StringBuilder();
        if (check(sb)) {
            return Response.ok().entity("All injected").build();
        } else {
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(sb.toString()).build();
        }
    }
}
