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

package org.glassfish.jersey.tests.integration.jersey2167;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test resource for JERSEY-2167 reproducer.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */

@Path("/MyResource")
public class Issue2167Resource {
    private static final Logger LOGGER = Logger.getLogger(Issue2167Resource.class.getName());
    public static final String MSG = "Resource method doA() called at ";

    @Path("test")
    @GET
    public Response doA(@MyAnnotation String param) {
        LOGGER.log(Level.INFO, MSG + System.currentTimeMillis() + "; param=" + param);
        if (param == null) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
