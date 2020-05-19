/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

/**
 * Resource to test CDI injection into JAX-RS resource via constructor parameter.
 *
 * @author Adam Lindenthal
 */
@Path("constructor")
@RequestScoped
public class ConstructorInjectionResource {

    private FormDataBean bean;

    public ConstructorInjectionResource() {
    }

    @Inject
    public ConstructorInjectionResource(@Valid @BeanParam final FormDataBean form) {
        bean = form;
    }

    @POST
    @Produces("text/plain")
    public String get() {
        return bean.getName() + ":" + bean.getAge() + ":"
                + bean.getInjectedBean().getMessage() + ":" + bean.getInjectedPath();
    }
}
