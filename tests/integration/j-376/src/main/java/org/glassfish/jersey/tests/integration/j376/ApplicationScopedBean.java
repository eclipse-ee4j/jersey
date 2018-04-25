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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;


/**
 * Test {@code ApplicationScoped} bean to be injected to the test resource, while another {@code RequestScoped}
 * bean being injected into this class.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@ApplicationScoped
public class ApplicationScopedBean {

    /** Request scoped bean injected via CDI */
    @Inject
    private SecondBean bean;

    /** JAX-RS {@code Context} injection of request {@code UriInfo} */
    @Context
    private UriInfo uri;

    private String name = "ApplicationScopedBean";

    public String getMessage() {
        return name + ":" + bean.getMessage();
    }

    public String getUri() {
        return uri.getPath();
    }
}
