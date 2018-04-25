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

package org.glassfish.jersey.tests.cdi.resources;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * JAX-RS resource that gets injected with a CDI managed bean,
 * that includes JAX-RS injection points. The very same bean
 * gets injected also to {@link FirstNonJaxRsBeanInjectedResource}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("non-jaxrs-bean-injected")
@RequestScoped
public class FirstNonJaxRsBeanInjectedResource {

    @Inject
    JaxRsInjectedBean bean;

    @GET
    @Path("path/1")
    public String getPath() {
        return bean.getUriInfo().getPath();
    }

    @GET
    @Path("header/1")
    public String getAcceptHeader() {
        return bean.getTestHeader();
    }
}
