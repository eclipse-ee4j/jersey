/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
    @Path("all/{pathParam}")
    @Produces("text/plain")
    public String echo(@BeanParam MyBeanParam bean);

    @Path("subresource")
    MyResourceWithBeanParamIfc getSubResource();

}