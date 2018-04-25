/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * Test for Response.close() method.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ResponseCloseTest extends JerseyTest {

    @Path("simple")
    public static class SimpleResource {

        @GET
        @Produces("text/plain")
        public String getIt() {
            return "it";
        }

        @GET
        @Path("empty")
        public Response getEmpty() {
            return Response.noContent().build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(SimpleResource.class);
    }

    @Test
    public void testReadAfterClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.close();
        try {
            response.readEntity(String.class);
            fail("IllegalStateException expected when reading entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testCloseBeforeReadingEmptyContentHasNoEffect() {
        final Response response = target().path("simple").path("empty").request().get(Response.class);
        response.close();
        try {
            response.readEntity(String.class);
            fail("IllegalStateException expected when reading entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testReadAfterMultipleClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.close();
        response.close();
        response.close();
        try {
            response.readEntity(String.class);
            fail("IllegalStateException expected when reading entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testReadBufferedEntityAfterClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.bufferEntity();
        response.close();
        try {
            response.readEntity(String.class);
            fail("IllegalStateException expected when reading a buffered entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testBufferEntityAfterClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.close();
        try {
            response.bufferEntity();
            fail("IllegalStateException expected when reading a buffered entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testGetEntityAfterClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.close();
        try {
            response.getEntity();
            fail("IllegalStateException expected when reading a buffered entity after response has been closed.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }

    @Test
    public void testHasEntityAfterClose() {
        final Response response = target().path("simple").request().get(Response.class);
        response.close();
        try {
            response.hasEntity();
            fail("IllegalStateException should have been caught inside hasEntity.");
        } catch (final IllegalStateException ex) {
            // expected
        }
    }
}
