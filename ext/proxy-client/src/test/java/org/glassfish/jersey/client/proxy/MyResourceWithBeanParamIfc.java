/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * @author Richard Obersheimer
 */
@Path("mybeanresource")
public interface MyResourceWithBeanParamIfc {

    @GET
    @Path("getQuery")
    @Produces("text/plain")
    public String echoQuery(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getHeader")
    @Produces("text/plain")
    public String echoHeader(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getPath/{pathParam}")
    @Produces("text/plain")
    public String echoPath(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getCookie")
    @Produces("text/plain")
    public String echoCookie(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getMatrix")
    @Produces("text/plain")
    public String echoMatrix(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getSubBean")
    @Produces("text/plain")
    public String echoSubBean(@BeanParam MyGetBeanParam bean);

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("getPrivateField")
    @Produces("text/plain")
    public String echoPrivateField(@BeanParam MyBeanParamWithPrivateField bean);

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("all/{pathParam}")
    @Produces("text/plain")
    public String echo(@BeanParam MyBeanParam bean);

    @Path("subresource")
    MyResourceWithBeanParamIfc getSubResource();

}