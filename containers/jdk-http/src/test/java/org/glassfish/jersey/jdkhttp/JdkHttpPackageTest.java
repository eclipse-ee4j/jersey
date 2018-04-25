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

package org.glassfish.jersey.jdkhttp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Jdk Http Container package scanning test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JdkHttpPackageTest extends AbstractJdkHttpServerTester {

    @Path("/packageTest")
    public static class TestResource {
        @GET
        public String get() {
            return "test";
        }

        @GET
        @Path("sub")
        public String getSub() {
            return "test-sub";
        }
    }

    @Test
    public void testJdkHttpPackage() {
        final ResourceConfig rc = new ResourceConfig();
        rc.packages(this.getClass().getPackage().getName());

        startServer(rc);

        WebTarget r = ClientBuilder.newClient().target(getUri().path("/").build());

        assertEquals("test", r.path("packageTest").request().get(String.class));
        assertEquals("test-sub", r.path("packageTest/sub").request().get(String.class));
        assertEquals(404, r.path("wrong").request().get(Response.class).getStatus());

    }
}
