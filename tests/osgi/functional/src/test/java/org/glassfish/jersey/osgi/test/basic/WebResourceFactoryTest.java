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

package org.glassfish.jersey.osgi.test.basic;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Michal Gajdos
 */
@RunWith(PaxExam.class)
public class WebResourceFactoryTest {

    private static final String CONTEXT = "/jersey";

    private static final URI baseUri = UriBuilder
            .fromUri("http://localhost")
            .port(Helper.getPort())
            .path(CONTEXT).build();

    @Configuration
    public static Option[] configuration() {
        List<Option> options = Helper.getCommonOsgiOptions();

        options.addAll(Helper.expandedList(
                // jersey-multipart dependencies
                mavenBundle().groupId("org.glassfish.jersey.ext").artifactId("jersey-proxy-client").versionAsInProject()));

        options = Helper.addPaxExamMavenLocalRepositoryProperty(options);
        return Helper.asArray(options);
    }

    @XmlRootElement
    public static class MyBean {
        public String name;
    }

    @Path("myresource")
    public static interface MyResourceIfc {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        String getIt();

        @POST
        @Consumes({MediaType.APPLICATION_XML})
        @Produces({MediaType.APPLICATION_XML})
        List<MyBean> postIt(List<MyBean> entity);

        @Path("{id}")
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        String getId(@PathParam("id") String id);

        @Path("query")
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        String getByName(@QueryParam("name") String name);

        @Path("subresource")
        MySubResourceIfc getSubResource();
    }

    public static class MyResource implements MyResourceIfc {

        @Override
        public String getIt() {
            return "Got it!";
        }

        @Override
        public List<MyBean> postIt(List<MyBean> entity) {
            return entity;
        }

        @Override
        public String getId(String id) {
            return id;
        }

        @Override
        public String getByName(String name) {
            return name;
        }

        @Override
        public MySubResourceIfc getSubResource() {
            return new MySubResource();
        }
    }

    public static class MySubResource implements MySubResourceIfc {

        @Override
        public MyBean getMyBean() {
            MyBean bean = new MyBean();
            bean.name = "Got it!";
            return bean;
        }
    }

    public static interface MySubResourceIfc {

        @GET
        @Produces(MediaType.APPLICATION_XML)
        public MyBean getMyBean();
    }

    private HttpServer server;
    private MyResourceIfc resource;

    @Before
    public void setUp() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
        resource = WebResourceFactory.newResource(MyResourceIfc.class, ClientBuilder.newClient().target(baseUri));
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void testGetIt() {
        assertEquals("Got it!", resource.getIt());
    }

    @Test
    public void testPostIt() {
        MyBean bean = new MyBean();
        bean.name = "Foo";
        assertEquals("Foo", resource.postIt(Collections.singletonList(bean)).get(0).name);
    }

    @Test
    public void testPathParam() {
        assertEquals("Bar", resource.getId("Bar"));
    }

    @Test
    public void testQueryParam() {
        assertEquals("Jersey2", resource.getByName("Jersey2"));
    }

    @Test
    public void testSubResource() {
        assertEquals("Got it!", resource.getSubResource().getMyBean().name);
    }
}
