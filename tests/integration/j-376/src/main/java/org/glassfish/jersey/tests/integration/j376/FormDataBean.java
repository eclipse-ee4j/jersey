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

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Test bean containingboth JAX-RS and CDI injection points.
 */
@RequestScoped
public class FormDataBean {

    private String injectedPath = null;

    @NotNull
    @Size(min = 4)
    @FormParam("name")
    private String name;

    @Min(18)
    @FormParam("age")
    private int age;

    @Inject
    private SecondBean injectedBean;

    @Context
    private UriInfo uri;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    /**
     * Exposes the state of injected {@code UriInfo} in the time of the call of {@link javax.annotation.PostConstruct}
     * annotated method. The returned value will be used in test to ensure, that {@code UriInfo} is injected in time
     *
     * @return path injected via {@code UriInfo} at the time-point of the {@link #postConstruct()} method call.

     */
    public String getInjectedPath() {
        return injectedPath;
    }

    public SecondBean getInjectedBean() {
        return injectedBean;
    }

    @PostConstruct
    public void postConstruct() {
        this.injectedPath = uri.getPath();
    }

}
