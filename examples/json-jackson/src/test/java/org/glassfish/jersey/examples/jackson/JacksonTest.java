/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.runner.RunWith;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(ConcurrentRunner.class)
public class JacksonTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return App.createApp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonFeature()).register(MyObjectMapperProvider.class);
    }

    @Test
    public void testEmptyArrayPresent() {
        WebTarget target = target();
        String responseMsg = target.path("emptyArrayResource").request(MediaType.APPLICATION_JSON).get(String.class);
        assertTrue(responseMsg.replaceAll("[ \t]*", "").contains("[]"));
    }

    @Test
    public void testJSONPPresent() {
        WebTarget target = target();
        String responseMsg = target.path("nonJaxbResource").request("application/javascript").get(String.class);
        assertTrue(responseMsg.startsWith("callback("));
    }

    @Test
    public void testJSONDoesNotReflectJSONPWrapper() {
        WebTarget target = target();
        String responseMsg = target.path("nonJaxbResource").request("application/json").get(String.class);
        assertTrue(!responseMsg.contains("jsonSource"));
    }

    @Test
    public void testCombinedAnnotationResource() {
        WebTarget target = target();
        String responseMsg = target.path("combinedAnnotations").request("application/json").get(String.class);
        assertTrue(responseMsg.contains("account") && responseMsg.contains("value"));
    }

    @Test
    public void testEmptyArrayBean() {
        WebTarget target = target();
        EmptyArrayBean responseMsg = target.path("emptyArrayResource").request(MediaType.APPLICATION_JSON)
                .get(EmptyArrayBean.class);
        assertNotNull(responseMsg);
    }

    @Test
    public void testCombinedAnnotationBean() {
        WebTarget target = target();
        CombinedAnnotationBean responseMsg = target.path("combinedAnnotations").request("application/json")
                .get(CombinedAnnotationBean.class);
        assertNotNull(responseMsg);
    }

    @Test
    @Ignore
    // TODO un-ignore once a JSON reader for "application/javascript" is supported
    public void testJSONPBean() {
        WebTarget target = target();
        NonJaxbBean responseMsg = target.path("nonJaxbResource").request("application/javascript").get(NonJaxbBean.class);
        assertNotNull(responseMsg);
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     * <p/>
     */
    @Test
    public void testApplicationWadl() {
        WebTarget target = target();
        String serviceWadl = target.path("application.wadl").request(MediaTypes.WADL_TYPE).get(String.class);

        assertTrue(serviceWadl.length() > 0);
    }

    /**
     * Test, that in case of malformed JSON, the jackson exception mappers will be used and the response will be
     * 400 - bad request instead of 500 - server error
     */
    @Test
    public void testExceptionMapping() {
        enable(TestProperties.LOG_TRAFFIC);
        // create a request with invalid json string to cause an exception in Jackson
        Response response = target().path("parseExceptionTest").request("application/json")
                .put(Entity.entity("Malformed json string.", MediaType.valueOf("application/json")));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}
