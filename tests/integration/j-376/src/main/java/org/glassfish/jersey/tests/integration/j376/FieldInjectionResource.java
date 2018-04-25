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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;

/**
 * Resource to test CDI injection into JAX-RS resource via field.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("field")
@RequestScoped
public class FieldInjectionResource {

    /** CDI injected request scoped field */
    @Inject
    @Valid
    @BeanParam
    private FormDataBean bean;

    /** CDI injected applciation scoped bean */
    @Inject
    private ApplicationScopedBean appScoped;

    /**
     * Return string containing of fields from the injected non JAX-RS request scoped bean,
     * path injected into it via {@code Context} annotation and another bean injected into it.
     *
     * Shows, that {@code Inject} and {@code Context} annotations can be used on one particular non JAX-RS class.
     **/
    @POST
    @Produces("text/plain")
    public String get() {
        return bean.getName() + ":" + bean.getAge() + ":"
                + bean.getInjectedBean().getMessage() + ":" + bean.getInjectedPath();
    }

    /** Return string from the {@code ApplicationScoped} non JAX_RS bean injected into this JAX-RS resource. */
    @GET
    @Path("appScoped")
    @Produces("text/plain")
    public String getMessage() {
        return appScoped.getMessage();
    }

    /**
     * Return path injected via {@code Context} annotation into {@code ApplicationScoped} non JAX-RS bean, that is
     * further injected into this JAX-RS resource via CDI.
     */
    @GET
    @Path("appScopedUri")
    public String getUri() {
        return appScoped.getUri();
    }
}
