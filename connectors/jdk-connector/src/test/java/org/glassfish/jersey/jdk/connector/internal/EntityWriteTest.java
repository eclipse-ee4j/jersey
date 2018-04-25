/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
public class EntityWriteTest extends JerseyTest {

    private static final String target = "entityWrite";

    @Override
    protected Application configure() {
        return new ResourceConfig(EchoResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.CHUNKED_ENCODING_SIZE, 20);
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Test
    public void testBuffered() {
        String message = TestUtils.generateBody(5000);
        Response response = target(target).request().post(Entity.entity(message, MediaType.TEXT_PLAIN));
        assertEquals(message, response.readEntity(String.class));
    }

    @Test
    public void testChunked() {
        String message = TestUtils.generateBody(5000);
        Response response = target(target).request()
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED)
                .post(Entity.entity(message, MediaType.TEXT_PLAIN));
        assertEquals(message, response.readEntity(String.class));
    }

    @Path("/entityWrite")
    public static class EchoResource {

        @POST
        public String post(String entity) {
            return entity;
        }
    }
}
