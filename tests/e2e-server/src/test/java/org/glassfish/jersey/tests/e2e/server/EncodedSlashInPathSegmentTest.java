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

package org.glassfish.jersey.tests.e2e.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test for JERSEY-1167.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class EncodedSlashInPathSegmentTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(EncodedSlashResource.class);
    }

    @Path("/test/{p}")
    public static class EncodedSlashResource {

        @GET
        public String get(@PathParam("p") String p) {
            return p;
        }
    }

    @Test
    public void testEncodedSlashInPathParam() throws Exception {

        final Response response = target().path("test/one%2Ftwo").request().get();

        assertEquals(200, response.getStatus());
        assertEquals("one/two", response.readEntity(String.class));
    }

}
