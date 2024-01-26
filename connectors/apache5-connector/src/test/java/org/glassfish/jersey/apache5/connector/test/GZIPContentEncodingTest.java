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

import java.util.Arrays;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Paul Sandoz
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class GZIPContentEncodingTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("/")
    public static class Resource {

        @POST
        public byte[] post(byte[] content) {
            return content;
        }
    }

    @Test
    public void testPost() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        byte[] content = new byte[1024 * 1024];
        assertTrue(Arrays.equals(content,
                r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE)).readEntity(byte[].class)));

        Response cr = r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertTrue(cr.hasEntity());
        cr.close();
    }

    @Test
    public void testPostChunked() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        cc.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
        cc.connectorProvider(new Apache5ConnectorProvider());
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());

        byte[] content = new byte[1024 * 1024];
        assertTrue(Arrays.equals(content,
                r.request().post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE)).readEntity(byte[].class)));

        Response cr = r.request().post(Entity.text("POST"));
        assertTrue(cr.hasEntity());
        cr.close();
    }

}
