/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.ejb.lib;

import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Singleton EJB counter bean as a JAX-RS resource.
 * The bean is for one published as a standalone JAX-RS resource
 * and for two used to inject other EJB based JAX-RS resources.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Singleton
@Path("counter")
public class EjbCounterResource {

    final AtomicInteger counter = new AtomicInteger();

    @Context UriInfo ui;

    @GET
    public int getCount() {
        return counter.incrementAndGet();
    }

    @Path("{ui}")
    @GET
    public String getUi() {
        return ui != null ? ui.getPath() : "UriInfo is null";
    }
}
