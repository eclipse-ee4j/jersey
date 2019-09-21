/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.apache.http.client.HttpRequestRetryHandler;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RetryHandlerTest extends JerseyTest {
    private static final int READ_TIMEOUT_MS = 100;

    @Override
    protected Application configure() {
        return new ResourceConfig(RetryHandlerResource.class);
    }

    @Path("/")
    public static class RetryHandlerResource {
        private static volatile int postRequestNumber = 0;
        private static volatile int getRequestNumber = 0;

        // Cause a timeout on the first GET and POST request
        @GET
        public String get(@Context HttpHeaders h) {
            if (getRequestNumber++ == 0) {
                try {
                    Thread.sleep(READ_TIMEOUT_MS * 10);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            if (postRequestNumber++ == 0) {
                try {
                    Thread.sleep(READ_TIMEOUT_MS * 10);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
            return "POST";
        }
    }

    @Test
    public void testRetryGet() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new ApacheConnectorProvider());
        cc.property(ApacheClientProperties.RETRY_HANDLER,
                (HttpRequestRetryHandler) (exception, executionCount, context) -> true);
        cc.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testRetryPost() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new ApacheConnectorProvider());
        cc.property(ApacheClientProperties.RETRY_HANDLER,
                (HttpRequestRetryHandler) (exception, executionCount, context) -> true);
        cc.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("POST", r.request()
                              .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED)
                              .post(Entity.text("POST"), String.class));
    }
}
