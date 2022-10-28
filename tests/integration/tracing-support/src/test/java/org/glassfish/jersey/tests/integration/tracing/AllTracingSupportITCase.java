/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.tracing;

import java.io.IOException;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.TracingConfig;
import org.glassfish.jersey.server.internal.ServerTraceEvent;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 'ALL' tracing support test that is running in external Jetty container.
 *
 * @author Libor Kramolis
 */
public class AllTracingSupportITCase extends JerseyTest {

    public static Stream<String> testData() {
        return Stream.of("/root", "/async");
    }

    //
    // JerseyTest
    //

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new AllTracingSupport();
    }

    @Override
    protected void configureClient(ClientConfig clientConfig) {
        Utils.configure(clientConfig);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    //
    // tests
    //

    @ParameterizedTest
    @MethodSource("testData")
    public void testGet(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("NAME").request();
        Response response = builder.get();
        assertXJerseyTrace(response, false);
        assertEquals(200, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testRuntimeException(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("runtime-exception").request();

        Response response = builder.get();
        assertXJerseyTrace(response, true);
        assertEquals(500, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testMappedException(String resourcePath) throws InterruptedException, IOException {
        Invocation.Builder builder = resource(resourcePath).path("mapped-exception").request();

        Response response = builder.get();
        assertXJerseyTrace(response, true);
        assertEquals(501, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testGet405(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).request();

        Response response = builder.get();
        assertXJerseyTrace(response, false);
        assertEquals(405, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPostSubResourceMethod(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("sub-resource-method").request();

        Response response = builder.post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertXJerseyTrace(response, false);
        assertEquals("TSOP", response.readEntity(Message.class).getText());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPostSubResourceLocator(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("sub-resource-locator").request();

        Response response = builder.post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertXJerseyTrace(response, false);
        assertEquals("TSOP", response.readEntity(Message.class).getText());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPostSubResourceLocatorNull(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("sub-resource-locator-null").request();

        Response response = builder.post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertEquals(404, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPostSubResourceLocatorSubResourceMethod(String resourcePath) {
        Invocation.Builder builder = resource(resourcePath).path("sub-resource-locator").path("sub-resource-method").request();

        Response response = builder.post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertXJerseyTrace(response, false);
        assertEquals("TSOP", response.readEntity(Message.class).getText());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testTraceInnerException(String resourcePath) {
        // PRE_MATCHING_REQUEST_FILTER
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_WEB_APPLICATION, 500, false);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_PROCESSING, 500, true);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_ANY, 500, true);
        // MESSAGE_BODY_WRITER
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_WRITER_THROW_WEB_APPLICATION, 500, false);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_WRITER_THROW_PROCESSING, 500, true);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_WRITER_THROW_ANY, 500, true);
        // MESSAGE_BODY_READER
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_READER_THROW_WEB_APPLICATION, 500, false);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_READER_THROW_PROCESSING, 500, true);
        testTraceInnerExceptionImpl(resourcePath, Utils.TestAction.MESSAGE_BODY_READER_THROW_ANY, 500, true);
    }

    //
    // utils
    //

    private void testTraceInnerExceptionImpl(String resourcePath, Utils.TestAction testAction,
            int expectedStatus, boolean exceptionExpected) {
        Invocation.Builder builder = resource(resourcePath).request();
        builder.header(Utils.HEADER_TEST_ACTION, testAction);

        Response response = builder.post(Entity.entity(new Message(testAction.name()), Utils.APPLICATION_X_JERSEY_TEST));
        assertXJerseyTrace(response, exceptionExpected);
        assertEquals(expectedStatus, response.getStatus());
    }

    private void assertXJerseyTrace(Response response, boolean exceptionExpected) {
        int finished = 0;
        int exceptionMapping = 0;

        for (String k : response.getHeaders().keySet()) {
            if (k.toLowerCase().startsWith(Utils.HEADER_TRACING_PREFIX.toLowerCase())) {
                String value = response.getHeaderString(k);
                if (value.startsWith(ServerTraceEvent.FINISHED.category())) {
                    finished++;
                } else if (value.startsWith(ServerTraceEvent.EXCEPTION_MAPPING.category())) {
                    exceptionMapping++;
                }
            }
        }
        assertEquals(1, finished, "Just one FINISHED expected!");
        if (exceptionExpected) {
            assertEquals(1, exceptionMapping, "EXCEPTION expected!");
        } else {
            assertEquals(0, exceptionMapping, "EXCEPTION NOT expected!");
        }
    }

    private WebTarget resource(String path) {
        return target("/" + TracingConfig.ALL).path(path);
    }

}
