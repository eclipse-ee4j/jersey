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
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests determining media type of the response (especially that qs quality parameter is respected when
 * more media types are defined on the resource method).
 *
 * @author Miroslav Fuksa
 */
public class ContentNegotiationTest extends JerseyTest {

    @Path("persons")
    public static class MyResource {
        private static final Person[] LIST = new Person[] {
                new Person("Penny", 1),
                new Person("Howard", 2),
                new Person("Sheldon", 3)
        };

        @GET
        @Produces({"application/xml;qs=0.75", "application/json;qs=1.0"})
        public Person[] getList() {
            return LIST;
        }

        @GET
        @Produces({"application/json;qs=1", "application/xml;qs=0.75"})
        @Path("reordered")
        public Person[] getListReordered() {
            return LIST;
        }

        @GET
        @Produces({"application/json;qs=0.75", "application/xml;qs=1"})
        @Path("inverted")
        public Person[] getListInverted() {
            return LIST;
        }


        @GET
        @Produces({"application/xml;qs=0.75", "application/json;qs=0.9", "unknown/hello;qs=1.0"})
        @Path("unkownMT")
        public Person[] getListWithUnkownType() {
            return LIST;
        }

        @GET
        @Produces({"application/json", "application/xml", "text/plain"})
        @Path("shouldPickFirstJson")
        public Person[] getJsonArrayUnlessOtherwiseSpecified() {
            return LIST;
        }

        @GET
        @Produces({"application/xml", "text/plain", "application/json"})
        @Path("shouldPickFirstXml")
        public Person[] getXmlUnlessOtherwiseSpecified() {
            return LIST;
        }

        @GET
        @Produces("application/json;qs=0.75")
        @Path("twoMethodsOneEndpoint")
        public Person[] getJsonArray() {
            return LIST;
        }

        @GET
        @Produces("application/xml;qs=1")
        @Path("twoMethodsOneEndpoint")
        public Person[] getXml() {
            return LIST;
        }
    }

    @XmlRootElement
    public static class Person {
        private String name;
        private int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(LoggingFeature.class);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    /**
     * {@link HttpUrlConnector} by default adds some media types
     * to the Accept header if we don't specify them.
     */
    @Test
    public void testWithoutDefinedRequestedMediaType() {
        WebTarget target = target().path("/persons");
        Response response = target.request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void testWithoutDefinedRequestedMediaTypeAndTwoMethods() {
        //We can not rely on method declaration ordering:
        //From Class javadoc: "The elements in the returned array are not sorted and are not in any particular order."
        //If there are same endpoints it is necessary to use quality parameter to ensure ordering.
        Response response = target().path("/persons/twoMethodsOneEndpoint").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, response.getMediaType());
    }

    @Test
    public void testWithoutDefinedRequestedMediaTypeOrQualityModifiersJson() {
        Response response = target().path("/persons/shouldPickFirstJson").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void testWithoutDefinedRequestedMediaTypeOrQualityModifiersXml() {
        Response response = target().path("/persons/shouldPickFirstXml").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, response.getMediaType());
    }

    @Test
    public void test() {
        WebTarget target = target().path("/persons");
        Response response = target.request(MediaType.WILDCARD).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void testInverted() {
        WebTarget target = target().path("/persons/inverted");
        Response response = target.request(MediaType.WILDCARD).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, response.getMediaType());
    }

    @Test
    public void testInvertedWithJSONPreferredByClient() {
        WebTarget target = target().path("/persons/inverted");
        Response response = target.request("application/json;q=1.0", "application/xml;q=0.8").get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    public void testReordered() {
        WebTarget target = target().path("/persons/reordered");
        Response response = target.request(MediaType.WILDCARD).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    /**
     * Client and server prefers "unknown/hello" but there is no MBW on server to write such a type. Therefore
     * this type is ignored and "application/xml" is chosen (because it is the second preferred type by the client).
     */
    @Test
    public void testWithUnknownTypePreferredByClient() {
        WebTarget target = target().path("/persons/reordered");
        Response response = target.request("application/json;q=0.8", "application/xml;q=0.9",
                "unknown/hello;qs=1.0").get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, response.getMediaType());
    }
}
