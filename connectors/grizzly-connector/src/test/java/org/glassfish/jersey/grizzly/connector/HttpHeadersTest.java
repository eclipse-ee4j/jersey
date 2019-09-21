/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly.connector;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the headers.
 *
 * @author Stepan Kopriva
 */
public class HttpHeadersTest extends JerseyTest{
    @Path("/test")
    public static class HttpMethodResource {
        @POST
        public String post(
                @HeaderParam("Transfer-Encoding") String transferEncoding,
                @HeaderParam("X-CLIENT") String xClient,
                @HeaderParam("X-WRITER") String xWriter,
                String entity) {
            assertEquals("client", xClient);
            return "POST";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpHeadersTest.HttpMethodResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new GrizzlyConnectorProvider());
    }

    @Test
    public void testPost() {
        Response response = target("test").request().header("X-CLIENT", "client").post(null);

        assertEquals(200, response.getStatus());
        assertTrue(response.hasEntity());
    }
}
