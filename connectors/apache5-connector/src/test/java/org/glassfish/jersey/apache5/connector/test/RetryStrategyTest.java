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

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetryStrategyTest extends JerseyTest {
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
        cc.connectorProvider(new Apache5ConnectorProvider());
        cc.property(Apache5ClientProperties.RETRY_STRATEGY,
                new HttpRequestRetryStrategy() {
                    @Override
                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                        return true;
                    }

                    @Override
                    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
                        return true;
                    }

                    @Override
                    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
                        return TimeValue.ofMilliseconds(200);
                    }
                });
        cc.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testRetryPost() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new Apache5ConnectorProvider());
        cc.property(Apache5ClientProperties.RETRY_STRATEGY,
                new HttpRequestRetryStrategy() {
                    @Override
                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                        return true;
                    }

                    @Override
                    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
                        return true;
                    }

                    @Override
                    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
                        return TimeValue.ofMilliseconds(200);
                    }
                });
        cc.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);

        WebTarget r = client.target(getBaseUri());
        assertEquals("POST", r.request()
                              .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED)
                              .post(Entity.text("POST"), String.class));
    }
}
