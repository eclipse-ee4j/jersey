/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * EJB backed JAX-RS resource injected with CDI service providers.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Stateful
@Path("stateful")
public class EjbStatefulResource {

    @Inject CdiRequestScopedTimer requestScopedTimer;
    @Inject CdiAppScopedTimer appScopedTimer;

    @GET
    @Path("request-scoped-timer")
    public String getReqTime() {
        return Long.toString(requestScopedTimer.getMiliseconds());
    }

    @GET
    @Path("app-scoped-timer")
    public String getAppTime() {
        return Long.toString(appScopedTimer.getMiliseconds());
    }
}
