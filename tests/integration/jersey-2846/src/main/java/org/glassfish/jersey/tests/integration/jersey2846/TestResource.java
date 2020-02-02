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

package org.glassfish.jersey.tests.integration.jersey2846;

import java.io.File;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * @author Michal Gajdos
 */
@Path("/")
@Consumes("multipart/form-data")
@Produces("text/plain")
public class TestResource {

    @POST
    @Path("ExceptionInMethod")
    public String exceptionInMethod(@FormDataParam("file") final File file) {
        throw new WebApplicationException(Response.serverError().entity(file.getAbsolutePath()).build());
    }

    @POST
    @Path("SuccessfulMethod")
    public String successfulMethod(@FormDataParam("file") final File file) {
        return file.getAbsolutePath();
    }
}
