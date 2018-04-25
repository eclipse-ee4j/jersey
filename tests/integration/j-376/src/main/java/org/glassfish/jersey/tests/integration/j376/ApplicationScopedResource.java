/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j376;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Resource to test CDI injection into {@code ApplicationScoped} JAX-RS resource.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("appScope")
@ApplicationScoped
public class ApplicationScopedResource {

    /** Application scoped CDI injected bean */
    @Inject
    private ApplicationScopedBean appScoped;

    /** Request scoped CDI injected bean */
    @Inject
    private SecondBean reqScoped;

    /** Bean containing form parameters injected by JAX-RS */
    @Inject
    @BeanParam
    private FormDataBean bean;

    @POST
    @Produces("text/plain")
    public String get() {
        return bean.getName() + ":" + bean.getAge() + ":"
                + bean.getInjectedBean().getMessage() + ":" + bean.getInjectedPath();
    }

    @GET
    @Path("msg")
    @Produces("text/plain")
    public String testAppScoped() {
        return appScoped.getMessage();
    }

    @GET
    @Path("uri")
    public String getUri() {
        return appScoped.getUri();
    }

    @GET
    @Path("req")
    public String getMessage() {
        return reqScoped.getMessage();
    }
}
