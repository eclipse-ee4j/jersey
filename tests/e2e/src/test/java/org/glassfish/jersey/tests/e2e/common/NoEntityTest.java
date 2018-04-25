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

package org.glassfish.jersey.tests.e2e.common;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests that no content type is sent when an entity is not present.
 *
 * @author Miroslav Fuksa
 */
public class NoEntityTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(MyResource.class, MoxyJsonFeature.class);
        return resourceConfig;
    }

    @XmlRootElement
    public static class MyEntity {

        @XmlAttribute
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Path("resource")
    public static class MyResource {

        @GET
        @Produces("application/json")
        @Path("no-entity")
        public Response getNoEntity() {
            return Response.status(204).build();
        }

        @GET
        @Produces("application/json")
        @Path("entity")
        public Response getEntity() {
            MyEntity myEntity = new MyEntity();
            myEntity.setName("hello");
            return Response.status(200).entity(myEntity).build();
        }

        @GET
        @Produces("text/plain")
        @Path("string")
        public Response getEmptyString() {

            return Response.status(204).entity("").build();
        }

    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(
                new LoggingFeature(Logger.getLogger(NoEntityTest.class.toString()), LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    /**
     * Tests that returned media type is null when no entity is sent.
     */
    @Test
    public void testNoEntity() {
        Response response = target().path("resource/no-entity").request(MediaType.APPLICATION_JSON_TYPE).get();
        MyEntity myEntity = response.readEntity(MyEntity.class);
        assertNull(myEntity);
        assertEquals(204, response.getStatus());
        assertNull(response.getMediaType());
    }

    /**
     * Tests that correct media type is returned.
     */
    @Test
    public void testEntity() {
        Response response = target().path("resource/entity").request(MediaType.APPLICATION_JSON_TYPE).get();
        MyEntity myEntity = response.readEntity(MyEntity.class);
        assertEquals("hello", myEntity.getName());
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    /**
     * Tests that entity is read as null when no entity is sent with 204 response status.
     * Currently this test throws an exception when trying to read the entity.
     * <p/>
     * Exception:
     * <p/>
     * org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException:
     * MessageBodyReader not found for media type=text/plain,
     * type=class org.glassfish.jersey.tests.e2e.common.NoEntityTest$MyEntity,
     * genericType=class org.glassfish.jersey.tests.e2e.common.NoEntityTest$MyEntity.
     * <p/>
     * https://java.net/jira/browse/JERSEY-1994
     */
    @Test
    @Ignore("see https://java.net/jira/browse/JERSEY-1994")
    public void testNoEntityString() {
        Response response = target().path("resource/string").request().get();
        MyEntity myEntity = response.readEntity(MyEntity.class);
        assertNull(myEntity);
        assertEquals(204, response.getStatus());
        assertEquals("text/plain", response.getMediaType());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new SimpleTestContainerFactory();
    }
}
