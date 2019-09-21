/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests custom response status reason phrase with jersey containers and connectors.
 *
 * @author Miroslav Fuksa
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ResponseStatusTypeTest.InMemoryTest.class,
        ResponseStatusTypeTest.GrizzlyContainerGrizzlyConnectorTest.class,
        ResponseStatusTypeTest.GrizzlyContainerApacheConnectorTest.class,
        ResponseStatusTypeTest.SimpleContainerHttpUrlConnectorTest.class})
public class ResponseStatusTypeTest {

    public static final String REASON_PHRASE = "my-phrase";

    public static class InMemoryTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(TestResource.class);

        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new InMemoryTestContainerFactory();
        }

        @Test
        public void testCustom() {
            _testCustom(target());
        }

        @Test
        public void testBadRequest() {
            _testBadRequest(target());
        }

        @Test
        public void testCustomBadRequest() {
            // with InMemory container and connector status info should be transferred as it is produced.
            final Response response = target().path("resource/custom-bad-request").request().get();
            Assert.assertEquals(400, response.getStatus());
            Assert.assertNull(response.getStatusInfo().getReasonPhrase());

        }

    }

    public static class GrizzlyContainerGrizzlyConnectorTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(TestResource.class);

        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new GrizzlyTestContainerFactory();
        }

        @Override
        protected void configureClient(ClientConfig config) {
            config.connectorProvider(new GrizzlyConnectorProvider());
        }


        @Test
        public void testCustom() {
            _testCustom(target());
        }

        @Test
        public void testBadRequest() {
            _testBadRequest(target());
        }

        @Test
        public void testCustomBadRequest() {
            _testCustomBadRequest(target());
        }
    }

    public static class GrizzlyContainerApacheConnectorTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(TestResource.class);

        }

        @Override
        protected void configureClient(ClientConfig config) {
            config.connectorProvider(new ApacheConnectorProvider());
        }


        @Test
        public void testCustom() {
            _testCustom(target());
        }

        @Test
        public void testBadRequest() {
            _testBadRequest(target());
        }

        @Test
        public void testCustomBadRequest() {
            _testCustomBadRequest(target());
        }
    }

    public static class SimpleContainerHttpUrlConnectorTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(TestResource.class);

        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new SimpleTestContainerFactory();
        }

        @Test
        public void testCustom() {
            _testCustom(target());
        }

        @Test
        public void testBadRequest() {
            _testBadRequest(target());
        }

        @Test
        public void testCustomBadRequest() {
            _testCustomBadRequest(target());
        }
    }

    public static class JdkHttpContainerHttpUrlConnectorTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(TestResource.class);

        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new JdkHttpServerTestContainerFactory();
        }

        @Test
        @Ignore("Jdk http container does not support custom response reason phrases.")
        public void testCustom() {
            _testCustom(target());
        }

        @Test
        public void testBadRequest() {
            _testBadRequest(target());
        }

        @Test
        public void testCustomBadRequest() {
            _testCustomBadRequest(target());
        }
    }

    @Path("resource")
    public static class TestResource {

        @GET
        @Path("custom")
        public Response testStatusType() {
            return Response.status(new Custom428Type()).build();
        }

        @GET
        @Path("bad-request")
        public Response badRequest() {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        @GET
        @Path("custom-bad-request")
        public Response customBadRequest() {
            return Response.status(new CustomBadRequestWithoutReasonType()).build();
        }

    }

    public static class Custom428Type implements Response.StatusType {
        @Override
        public int getStatusCode() {
            return 428;
        }

        @Override
        public String getReasonPhrase() {
            return REASON_PHRASE;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }
    }

    public static class CustomBadRequestWithoutReasonType implements Response.StatusType {
        @Override
        public int getStatusCode() {
            return 400;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }
    }

    public static void _testCustom(WebTarget target) {
        final Response response = target.path("resource/custom").request().get();
        Assert.assertEquals(428, response.getStatus());
        Assert.assertEquals(REASON_PHRASE, response.getStatusInfo().getReasonPhrase());
    }

    public static void _testBadRequest(WebTarget target) {
        final Response response = target.path("resource/bad-request").request().get();
        Assert.assertEquals(400, response.getStatus());
        Assert.assertEquals(Response.Status.BAD_REQUEST.getReasonPhrase(), response.getStatusInfo().getReasonPhrase());
    }

    public static void _testCustomBadRequest(WebTarget target) {
        final Response response = target.path("resource/custom-bad-request").request().get();
        Assert.assertEquals(400, response.getStatus());
        Assert.assertEquals(Response.Status.BAD_REQUEST.getReasonPhrase(), response.getStatusInfo().getReasonPhrase());
    }
}
