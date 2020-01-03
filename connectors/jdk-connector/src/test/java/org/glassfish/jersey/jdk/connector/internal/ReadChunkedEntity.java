/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

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
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Petr Janouch
 *
 * TODO I have a strong feeling a got inspired somewhere, but forgot where.
 */
public class ReadChunkedEntity extends JerseyTest {

    @Path("/chunkedEntity")
    public static class ChunkedResource {

        @POST
        public ChunkedOutput<String> get(final String entity) {
            final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

            new Thread() {
                public void run() {
                    try {
                        int startIdx = 0;
                        int remaining = entity.length();
                        while (remaining >= 0) {
                            int chunkLength = 10;
                            int endIdx = startIdx + chunkLength;
                            if (endIdx > entity.length()) {
                                endIdx = entity.length();
                            }
                            String chunk = entity.substring(startIdx, endIdx);
                            output.write(chunk);
                            remaining -= chunkLength;
                            startIdx = endIdx;
                        }
                    } catch (IOException e) {
                        //
                    } finally {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }.start();

            return output;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ChunkedResource.class);
    }

    @Test
    public void testChunked() {
        String message = TestUtils.generateBody(500);
        Response response = target("chunkedEntity").property(ClientProperties.REQUEST_ENTITY_PROCESSING,
                RequestEntityProcessing.CHUNKED).request().post(Entity.entity(message, MediaType.TEXT_PLAIN));
        assertEquals(message, response.readEntity(String.class));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.CHUNKED_ENCODING_SIZE, 20);
        config.connectorProvider(new JdkConnectorProvider());
    }
}
