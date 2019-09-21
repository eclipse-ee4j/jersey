/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxrstypeinjection;

import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class JaxrsTypeInjectionTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return App.create();
    }

    private String[] expectedFragmentsProgrammatic = new String[]{
            // UriInfo
            "Absolute path : " + this.getBaseUri() + "programmatic/v1/v2",
            "Base URI : " + this.getBaseUri(),
            "Path : programmatic/v1/v2",
            "Path segments : [programmatic, v1, v2]",
            "p1 : v1", "p2 : v2", // path params
            "q1 : 1", "q2 : v2, v3", // query params
            "Request URI : " + this.getBaseUri() + "programmatic/v1/v2?q1=1&q2=v2&q2=v3",
            // RequestHeaders/HttpHeaders
            "Accept : text/plain",
            // Injected Parameters
            "String path param p1=v1",
            "PathSegment path param p2=v2",
            "int query param q1=1",
            "List<String> query param q2=[v2, v3]"
    };
    private String[] expectedFragmentsAnnotatedInstance = new String[]{
            // UriInfo
            "Absolute path : " + this.getBaseUri() + "annotated/instance/v1/v2",
            "Base URI : " + this.getBaseUri(),
            "Path : annotated/instance/v1/v2",
            "Path segments : [annotated, instance, v1, v2]",
            "p1 : v1", "p2 : v2", // path params
            "q1 : 1", "q2 : v2, v3", // query params
            "Request URI : " + this.getBaseUri() + "annotated/instance/v1/v2?q1=1&q2=v2&q2=v3",
            // RequestHeaders/HttpHeaders
            "Accept : text/plain",
            // Injected Parameters
            "String path param p1=v1",
            "PathSegment path param p2=v2",
            "int query param q1=1",
            "List<String> query param q2=[v2, v3]"
    };
    private String[] expectedFragmentsAnnotatedMethod = new String[]{
            // UriInfo
            "Absolute path : " + this.getBaseUri() + "annotated/method/v1/v2",
            "Base URI : " + this.getBaseUri(),
            "Path : annotated/method/v1/v2",
            "Path segments : [annotated, method, v1, v2]",
            "p1 : v1", "p2 : v2", // path params
            "q1 : 1", "q2 : v2, v3", // query params
            "Request URI : " + this.getBaseUri() + "annotated/method/v1/v2?q1=1&q2=v2&q2=v3",
            // RequestHeaders/HttpHeaders
            "Accept : text/plain",
            // Injected Parameters
            "String path param p1=v1",
            "PathSegment path param p2=v2",
            "int query param q1=1",
            "List<String> query param q2=[v2, v3]"
    };

    private WebTarget prepareTarget(String path) {
        final WebTarget target = target();
        target.register(LoggingFeature.class);
        return target.path(path).resolveTemplate("p1", "v1").resolveTemplate("p2",
                "v2").queryParam("q1", 1).queryParam("q2", "v2").queryParam("q2", "v3");
    }

    @Test
    public void testProgrammaticApp() throws Exception {
        String responseEntity = prepareTarget(App.ROOT_PATH_PROGRAMMATIC).request("text/plain").get(String.class)
                .toLowerCase();

        for (String expectedFragment : expectedFragmentsProgrammatic) {
            assertTrue("Expected fragment '" + expectedFragment + "' not found in response:\n" + responseEntity,
                    // http header field names are case insensitive
                    responseEntity.contains(expectedFragment.toLowerCase()));
        }
    }

    @Test
    public void testAnnotatedInstanceApp() throws Exception {
        String responseEntity = prepareTarget(App.ROOT_PATH_ANNOTATED_INSTANCE).request("text/plain").get(String.class)
                .toLowerCase();

        for (String expectedFragment : expectedFragmentsAnnotatedInstance) {
            assertTrue("Expected fragment '" + expectedFragment + "' not found in response:\n" + responseEntity,
                    // http header field names are case insensitive
                    responseEntity.contains(expectedFragment.toLowerCase()));
        }
    }

    @Test
    public void testAnnotatedMethodApp() throws Exception {
        String responseEntity = prepareTarget(App.ROOT_PATH_ANNOTATED_METHOD).request("text/plain").get(String.class)
                .toLowerCase();

        for (String expectedFragment : expectedFragmentsAnnotatedMethod) {
            assertTrue("Expected fragment '" + expectedFragment + "' not found in response:\n" + responseEntity,
                    // http header field names are case insensitive
                    responseEntity.contains(expectedFragment.toLowerCase()));
        }
    }
}
