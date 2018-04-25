/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests that the MultivaluedMap injection does not close the request buffer and allows
 * to proceed other FormParam injections.
 *
 * @author Petr Bouda
 */
public class FormParamMultivaluedInjectionTest extends JerseyTest {

    public static final String PREDEFINED_RESPONSE = "Hello George Javatar";

    @Path("form")
    public static class FormResource {

        @POST
        @Path("simple")
        public Response simple(MultivaluedMap<String, String> formParams,
                              @FormParam("firstname") String firstname,
                              @FormParam("lastname") String lastname) {
            assertEquals(2, formParams.size());
            assertEquals("George", formParams.get("firstname").get(0));
            assertEquals("Javatar", formParams.get("lastname").get(0));
            return Response.status(Response.Status.OK).entity("Hello " + firstname + " " + lastname).build();
        }

        @POST
        @Path("nullable")
        public Response nullable(MultivaluedMap<String, String> formParams,
                              @FormParam("firstname") String firstname,
                              @FormParam("lastname") String lastname) {
            assertEquals(2, formParams.size());
            assertEquals(2, formParams.get("firstname").size());
            assertEquals("George", formParams.get("firstname").get(0));
            assertEquals("Javatar", formParams.get("lastname").get(0));
            return Response.status(Response.Status.OK).entity("Hello " + firstname + " " + lastname).build();
        }

        @POST
        @Path("mixed")
        public Response mixed(@FormParam("firstname") String firstname,
                              MultivaluedMap<String, String> formParams,
                              @FormParam("lastname") String lastname) {
            assertEquals(2, formParams.size());
            assertEquals(2, formParams.get("firstname").size());
            assertEquals("George", formParams.get("firstname").get(0));
            assertEquals("Javatar", formParams.get("lastname").get(0));
            return Response.status(Response.Status.OK).entity("Hello " + firstname + " " + lastname).build();
        }

        @POST
        @Path("encoded")
        public Response encoded(MultivaluedMap<String, String> formParams,
                               @Encoded @FormParam("firstname") String firstname,
                               @FormParam("lastname") String lastname) {
            assertEquals(2, formParams.size());
            assertEquals("George", formParams.get("firstname").get(0));
            assertEquals("Javatar", formParams.get("lastname").get(0));
            return Response.status(Response.Status.OK).entity("Hello " + firstname + " " + lastname).build();
        }

    }

    @Path("form-ext")
    public static class FormExtResource {

        @Encoded @FormParam("firstname") String firstname;

        @POST
        @Path("encoded")
        public Response encoded(MultivaluedMap<String, String> formParams,
                               @FormParam("lastname") String lastname) {
            assertEquals(2, formParams.size());
            assertEquals("George", formParams.get("firstname").get(0));
            assertEquals("Javatar", formParams.get("lastname").get(0));
            return Response.status(Response.Status.OK).entity("Hello " + firstname + " " + lastname).build();
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(FormResource.class, FormExtResource.class);
    }

    @Test
    public void testFormMultivaluedParam() {
        Response result = call("/form/simple", "firstname=George&lastname=Javatar");
        assertEquals(PREDEFINED_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testFormMultivaluedParamWithNull() {
        Response result = call("/form/nullable", "firstname=George&firstname&lastname=Javatar");
        assertEquals(PREDEFINED_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testFormMultivaluedParamMixedParamOrder() {
        Response result = call("/form/mixed", "firstname=George&firstname&lastname=Javatar");
        assertEquals(PREDEFINED_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testFormMultivaluedParamEncoded() {
        Response result = call("/form/encoded", "firstname=George&lastname=Javatar");
        assertEquals(PREDEFINED_RESPONSE, result.readEntity(String.class));
    }

    @Test
    public void testFormMultivaluedParamExternalEncodedInjection() {
        Response result = call("/form-ext/encoded", "firstname=George&lastname=Javatar");
        assertEquals(PREDEFINED_RESPONSE, result.readEntity(String.class));
    }

    private Response call(String path, String entity) {
        return target().path(path).request()
                .post(Entity.entity(entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    }
}

