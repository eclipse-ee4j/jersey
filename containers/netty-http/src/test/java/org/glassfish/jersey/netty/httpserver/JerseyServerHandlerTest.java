/*
 * Copyright (c) 2020, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JerseyServerHandlerTest extends AbstractNettyServerTester {

    private Client client;

    @Before
    public void setUp() throws Exception {
        startServer();
        client = ClientBuilder.newClient();
    }

    @Test
    public void testWithPathAndSlash() {
        Response r = client.target(getUri().path("resource/ping/")).request().get();
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals((Integer) 1, r.readEntity(Integer.class));
    }

    @Test
    public void testWithPath() {
        Response r = client.target(getUri().path("resource")).request().get();
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals((Integer) 2, r.readEntity(Integer.class));
    }

    @Test
    public void testWithoutClassPath() {
        Response r = client.target(getUri().path("ping")).request().get();
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals((Integer) 3, r.readEntity(Integer.class));
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        client = null;
    }
}
