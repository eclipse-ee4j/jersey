/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector.test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
        config.connectorProvider(new Apache5ConnectorProvider());
    }


    @Test
    @Disabled("Apache connector does not provide information about encoding for gzip and deflate encoding")
    public void testEncoded() {
        final Response response = target().path("resource/encoded").request("text/plain").get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("get", response.readEntity(String.class));
        Assertions.assertEquals("gzip", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        Assertions.assertEquals("text/plain", response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals(3, response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
    }

    @Test
    public void testNonEncoded() {
        final Response response = target().path("resource/non-encoded").request("text/plain").get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("get", response.readEntity(String.class));
        Assertions.assertNull(response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        Assertions.assertEquals("text/plain", response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals("3", response.getHeaderString(HttpHeaders.CONTENT_LENGTH));
    }
}
