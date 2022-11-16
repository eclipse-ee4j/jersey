/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

public class Issue4097Test extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Issue4097Resource.class);
    }

    @Test
    public void reuseResponse() throws InterruptedException {
        WebTarget webTarget = target("/issue4097/reuseResponse");
        assertEquals(410, webTarget.request().get().getStatus());
        assertEquals(500, webTarget.request().get().getStatus());
        assertEquals(500, webTarget.request().get().getStatus());
    }

    @Path("/issue4097")
    public static class Issue4097Resource {

        private static final Response RESPONSE =
            Response.status(410).entity("test").build();

        @GET
        @Path("/reuseResponse")
        public Response reuseResponse() {
            // returning the same response is obviously wrong
            return RESPONSE;
        }
    }

}
