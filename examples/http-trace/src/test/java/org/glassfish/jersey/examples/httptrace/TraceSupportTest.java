/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httptrace;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TraceSupportTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
        return App.create();
    }

    private String[] expectedFragmentsProgrammatic = new String[]{
            "TRACE http://localhost:" + this.getPort() + "/tracing/programmatic"
    };
    private String[] expectedFragmentsAnnotated = new String[]{
            "TRACE http://localhost:" + this.getPort() + "/tracing/annotated"
    };

    private WebTarget prepareTarget(String path) {
        final WebTarget target = target();
        target.register(LoggingFeature.class);
        return target.path(path);
    }

    @Test
    public void testProgrammaticApp() throws Exception {
        Response response = prepareTarget(App.ROOT_PATH_PROGRAMMATIC).request("text/plain").method(TRACE.NAME);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());

        String responseEntity = response.readEntity(String.class);
        for (String expectedFragment : expectedFragmentsProgrammatic) {
            assertTrue("Expected fragment '" + expectedFragment + "' not found in response:\n" + responseEntity,
                    // toLowerCase - http header field names are case insensitive
                    responseEntity.contains(expectedFragment));
        }
    }

    @Test
    public void testAnnotatedApp() throws Exception {
        Response response = prepareTarget(App.ROOT_PATH_ANNOTATED).request("text/plain").method(TRACE.NAME);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());

        String responseEntity = response.readEntity(String.class);
        for (String expectedFragment : expectedFragmentsAnnotated) {
            assertTrue("Expected fragment '" + expectedFragment + "' not found in response:\n" + responseEntity,
                    // toLowerCase - http header field names are case insensitive
                    responseEntity.contains(expectedFragment));
        }
    }

    @Test
    public void testTraceWithEntity() throws Exception {
        _testTraceWithEntity(false, false);
    }

    @Test
    public void testAsyncTraceWithEntity() throws Exception {
        _testTraceWithEntity(true, false);
    }

    @Test
    public void testTraceWithEntityGrizzlyConnector() throws Exception {
        _testTraceWithEntity(false, true);
    }

    @Test
    public void testAsyncTraceWithEntityGrizzlyConnector() throws Exception {
        _testTraceWithEntity(true, true);
    }

    private void _testTraceWithEntity(final boolean isAsync, final boolean useGrizzlyConnection) throws Exception {
        try {
            WebTarget target = useGrizzlyConnection ? getGrizzlyClient().target(target().getUri()) : target();
            target = target.path(App.ROOT_PATH_ANNOTATED);

            final Entity<String> entity = Entity.entity("trace", MediaType.WILDCARD_TYPE);

            Response response;
            if (!isAsync) {
                response = target.request().method(TRACE.NAME, entity);
            } else {
                response = target.request().async().method(TRACE.NAME, entity).get();
            }

            fail("A TRACE request MUST NOT include an entity. (response=" + response + ")");
        } catch (Exception e) {
            // OK
        }
    }

    private Client getGrizzlyClient() {
        return ClientBuilder.newClient(new ClientConfig().connectorProvider(new GrizzlyConnectorProvider()));
    }
}
