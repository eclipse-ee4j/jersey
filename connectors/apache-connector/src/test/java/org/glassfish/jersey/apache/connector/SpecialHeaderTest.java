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

package org.glassfish.jersey.apache.connector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Miroslav Fuksa
 */
public class SpecialHeaderTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class, GZipEncoder.class, LoggingFeature.class);
    }

    @Path("resource")
    public static class MyResource {
        @GET
        @Produces("text/plain")
        @Path("encoded")
        public Response getEncoded() {
            return Response.ok("get").header(HttpHeaders.CONTENT_ENCODING, "gzip").build();
        }

        @GET
        @Produces("text/plain")
        @Path("non-encoded")
        public Response getNormal() {
            return Response.ok("get").build();
        }
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new ApacheConnectorProvider());
    }


    @Test
    @Ignore("Apache connector does not provide information about encoding for gzip and deflate encoding")
    public void testEncoded() {
        final Response response = target().path("resource/encoded").request("text/plain").get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
        Assert.assertEquals("gzip", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/plain", response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        Assert.assertEquals(3, response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
    }

    @Test
    public void testNonEncoded() {
        final Response response = target().path("resource/non-encoded").request("text/plain").get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("get", response.readEntity(String.class));
        Assert.assertNull(response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/plain", response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        Assert.assertEquals("3", response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
    }
}
