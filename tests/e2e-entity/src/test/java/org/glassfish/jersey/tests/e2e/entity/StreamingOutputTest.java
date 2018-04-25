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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
@Path("/")
public class StreamingOutputTest extends JerseyTest {
    @GET
    @Path("wae")
    public StreamingOutput test2() {
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException {
                throw new WebApplicationException(Response.Status.BAD_GATEWAY.getStatusCode());
            }
        };
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(StreamingOutputTest.class);
    }

    @Test
    public void testWebApplicationException() {
        Response r = target("wae").request().get();
        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), r.getStatusInfo().getStatusCode());
    }
}
