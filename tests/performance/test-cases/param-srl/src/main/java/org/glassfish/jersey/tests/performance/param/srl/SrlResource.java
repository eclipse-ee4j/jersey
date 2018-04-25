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

package org.glassfish.jersey.tests.performance.param.srl;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Test resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/")
public class SrlResource {

    public static class SubResource {

        String p;

        SubResource(String p) {
            this.p = p;
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get(@MatrixParam("m") final String m, @QueryParam("q") final String q) {
            return String.format("p=%s, m=%s, q=%s", p, m, q);
        }
    }

    @Path("srl/{p}")
    public SubResource locator(@PathParam("p") String p) {
        return new SubResource(p);
    }

    @GET
    @Path("srm/{p}")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@PathParam("p") final String p, @MatrixParam("m") final String m, @QueryParam("q") final String q) {
        return String.format("p=%s, m=%s, q=%s", p, m, q);
    }
}
