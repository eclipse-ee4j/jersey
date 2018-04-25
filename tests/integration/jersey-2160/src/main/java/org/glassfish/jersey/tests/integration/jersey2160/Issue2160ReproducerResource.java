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

package org.glassfish.jersey.tests.integration.jersey2160;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.hk2.api.ProxyCtl;

/**
 * Test resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("servletInjectees")
public class Issue2160ReproducerResource {

    @Context
    HttpServletRequest requestField;
    @Context
    HttpServletResponse responseField;

    @GET
    @Produces("text/plain")
    public String ensureNoProxyInjected(@Context HttpServletRequest requestParam, @Context HttpServletResponse responseParam) {

        // make sure the injectees are same no matter how they got injected
        if (requestParam != requestField || responseParam != responseField) {
            throw new IllegalArgumentException("injected field and parameter should refer to the same instance");
        }

        // make sure we have not got proxies
        if (requestParam instanceof ProxyCtl || responseParam instanceof ProxyCtl) {
            throw new IllegalArgumentException("no proxy expected!");
        }

        return (String) requestParam.getAttribute(RequestFilter.REQUEST_NUMBER_PROPERTY);
    }
}
