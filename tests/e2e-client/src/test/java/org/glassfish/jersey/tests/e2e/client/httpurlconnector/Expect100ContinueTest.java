/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.httpurlconnector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.http.Expect100ContinueFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class Expect100ContinueTest extends JerseyTest {

    private static final String RESOURCE_PATH = "expect";
    private static final String ENTITY_STRING = "1234567890123456789012345678901234567890123456789012"
           + "3456789012345678901234567890";


    @Path(RESOURCE_PATH)
    public static class Expect100ContinueResource {

        /**
         * Disclamer - in tests we do not process 100-Continue response properly, so we operate 204 (no content)
         * and 200 (ok) response codes in order to distinguish between proper and not proper Expect:100-Continue
         * request handling.
         *
         * @param expect - Header value for Expect
         *
         * @return 200 (no expectations), 204 (Expect:100-Continue header processed)
         */
        @POST
        public Response publishResource(@HeaderParam("Expect") String expect) {
            if ("100-Continue".equalsIgnoreCase(expect)) {
                return Response.noContent().build();
            }
            return Response.ok().build();
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Expect100ContinueTest.Expect100ContinueResource.class);
    }

    @Test
    public void testExpect100Continue() {
       final Response response =  target(RESOURCE_PATH).request().post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 200", 200, response.getStatus()); //no Expect header sent - responce OK
    }

    @Test
    public void testExpect100ContinueChunked() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
               .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
               RequestEntityProcessing.CHUNKED).request().post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 204", 204, response.getStatus()); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueBuffered() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.basic())
               .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
               RequestEntityProcessing.BUFFERED).request().header(HttpHeaders.CONTENT_LENGTH, 67000L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 204", 204, response.getStatus()); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueCustomLength() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
               .request().header(HttpHeaders.CONTENT_LENGTH, 101L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 204", 204, response.getStatus()); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueCustomLengthWrong() {
       final Response response =  target(RESOURCE_PATH).register(Expect100ContinueFeature.withCustomThreshold(100L))
               .request().header(HttpHeaders.CONTENT_LENGTH, 99L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 200", 200, response.getStatus()); //Expect header NOT sent - low request size
    }

    @Test
    public void testExpect100ContinueCustomLengthProperty() {
       final Response response =  target(RESOURCE_PATH)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 555L)
               .register(Expect100ContinueFeature.basic())
               .request().header(HttpHeaders.CONTENT_LENGTH, 666L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 204", 204, response.getStatus()); //Expect header sent - No Content response
    }

    @Test
    public void testExpect100ContinueRegisterViaCustomProperty() {
       final Response response =  target(RESOURCE_PATH)
               .property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, 43L)
               .property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE)
               .request().header(HttpHeaders.CONTENT_LENGTH, 44L)
               .post(Entity.text(ENTITY_STRING));
       assertEquals("Expected 204", 204, response.getStatus()); //Expect header sent - No Content response
    }
}
