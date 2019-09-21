/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2704;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import javax.inject.Inject;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * This resource is used to test if specific service class instance is available in the
 * {@link InjectionManager} that comes from Jersey context.
 *
 * @author Bartosz Firyn (bartoszfiryn at gmail.com)
 */
@Path("test")
public class TestResource {

    InjectionManager injectionManager;

    /**
     * Inject {@link InjectionManager} from Jersey context.
     *
     * @param injectionManager the {@link InjectionManager}
     */
    @Inject
    public TestResource(InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    /**
     * This method will test given class by checking if it is available in {@link InjectionManager}
     * that has been injected from the Jersey context.
     *
     * @param clazz the service class name to check
     * @return {@link Response} with status code 200 if service is available, 600 otherwise
     * @throws Exception in case when there are any error (e.g. class not exist)
     */
    @GET
    @Path("{clazz}")
    @Produces("text/plain")
    public Response test(@PathParam("clazz") String clazz) throws Exception {
        return Response
            .status(injectionManager.getInstance(Class.forName(clazz)) != null ? 200 : 600)
            .build();
    }
}
