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

package org.glassfish.jersey.tests.integration.servlet_25_init_1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.servlet.WebConfig;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Martin Matula
 */
@Path("helloworld")
public class HelloWorldResource {

    @GET
    @Produces("text/plain")
    public String get() {
        return "Hello World! " + this.getClass().getPackage().getName();
    }

    @GET
    @Path("injection")
    public String getInjection(@Context HttpServletRequest request, @Context HttpServletResponse response,
                               @Context WebConfig webConfig, @Context ServletConfig servletConfig,
                               @Context ServletContext servletContext) {
        return request.getMethod() + (response != null) + webConfig.getName() + servletConfig.getServletName()
                + servletContext.getServletContextName();
    }
}
