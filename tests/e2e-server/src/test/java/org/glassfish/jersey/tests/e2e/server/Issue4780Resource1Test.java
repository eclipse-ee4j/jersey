/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class Issue4780Resource1Test extends JerseyTest {

    // 1 interface and 1 implementation having same @Path
    @Test
    public void resource1() throws Exception {
        Response response = target().path("/resource1").request().get();
        response.bufferEntity();
        assertEquals(response.readEntity(String.class), 200, response.getStatus());
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource1_1.class, IResource1_2.class);
    }

    @Path("")
    public static class Resource1_1 implements IResource1_2 {
        @GET
        @Path("/resource1")
        @Override
        public String get() {
            return "";
        }
    }

    @Path("")
    public static interface IResource1_2 {
        @GET
        @Path("/resource1")
        String get();
    }

}
