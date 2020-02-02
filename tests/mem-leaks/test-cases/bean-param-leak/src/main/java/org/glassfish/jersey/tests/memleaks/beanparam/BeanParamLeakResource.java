/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.memleaks.beanparam;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;

import javax.inject.Singleton;

/**
 * This resource reproduces JERSEY-2800 when {@link #invokeBeanParamInject()} called repetitively.
 *
 * @author Stepan Vavra
 */
@Path("/beanparam")
public class BeanParamLeakResource {

    public static class ParameterBean {
        @Context
        Request request;

        @QueryParam("q")
        String q;
    }

    @BeanParam
    ParameterBean bean;

    @POST
    @Path("invoke")
    public String invokeBeanParamInject() {
        return bean.q;
    }

    @GET
    @Path("helloworld")
    @Produces("text/plain")
    public String helloWorld() {
        return "HELLO WORLD!";
    }

}
