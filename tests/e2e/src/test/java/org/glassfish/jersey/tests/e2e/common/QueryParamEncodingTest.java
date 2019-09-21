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

package org.glassfish.jersey.tests.e2e.common;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miroslav Fuksa
 *
 */
public class QueryParamEncodingTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Path("resource")
    public static class TestResource {
        @GET
        @Path("encoded")
        public String getEncoded(@Encoded @QueryParam("query") String queryParam) {
            return queryParam.equals("%25dummy23%2Ba") + ":" + queryParam;
        }

        @GET
        @Path("decoded")
        public String getDecoded(@QueryParam("query") String queryParam) {
            return queryParam.equals("%dummy23+a") + ":" + queryParam;
        }
    }

    @Test
    public void testEncoded() {
        final Response response = target().path("resource/encoded").queryParam("query", "%dummy23+a").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("true:%25dummy23%2Ba", response.readEntity(String.class));
    }

    @Test
    public void testDecoded() {
        final Response response = target().path("resource/decoded").queryParam("query", "%dummy23+a").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("true:%dummy23+a", response.readEntity(String.class));
    }
}
