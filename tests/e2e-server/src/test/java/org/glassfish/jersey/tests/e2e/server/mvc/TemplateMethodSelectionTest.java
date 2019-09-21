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

package org.glassfish.jersey.tests.e2e.server.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests that {@link Template} annotated methods are selected by the routing algorithms as if they
 * would actually return {@link Viewable} instead of the model.
 *
 * @author Miroslav Fuksa
 */
public class TemplateMethodSelectionTest extends JerseyTest {

    private static final Map<String, String> MODEL = new HashMap<String, String>() {{
        put("a", "hello");
        put("b", "world");
    }};


    @Override
    protected Application configure() {
        return new ResourceConfig(
                TemplateAnnotatedResourceMethod.class,
                TemplateAnnotatedResource.class,
                BasicResource.class,
                AsViewableResource.class,
                NoTemplateResource.class,
                LoggingFeature.class,
                MvcFeature.class,
                TestViewProcessor.class,
                MoxyJsonFeature.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(MoxyJsonFeature.class);
    }

    public static MyBean getMyBean() {
        final MyBean myBean = new MyBean();
        myBean.setName("hello");
        return myBean;
    }

    @XmlRootElement
    public static class MyBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    @Path("annotatedMethod")
    public static class TemplateAnnotatedResourceMethod {

        @GET
        @Produces(MediaType.TEXT_HTML)
        @Template()
        public Map<String, String> getAsHTML() {
            return MODEL;
        }

        @GET
        @Produces("application/json")
        public MyBean getAsJSON() {
            return getMyBean();
        }
    }

    @Path("noTemplate")
    public static class NoTemplateResource {

        @GET
        @Produces(MediaType.TEXT_HTML)
        public Map<String, String> getAsHTML() {
            return MODEL;
        }

        @GET
        @Produces("application/json")
        public MyBean getAsJSON() {
            return getMyBean();
        }
    }

    @Path("annotatedClass")
    @Template
    @Produces(MediaType.TEXT_HTML)
    public static class TemplateAnnotatedResource {

        @GET
        @Produces("application/json")
        public MyBean getAsJSON() {
            return getMyBean();
        }

        @Override
        public String toString() {
            return "This toString() method will be used to get model.";
        }
    }

    @Path("basic")
    public static class BasicResource {
        @GET
        @Produces(MediaType.TEXT_HTML)
        public String getAsHTML() {
            return "Hello World";
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public MyBean getAsJSON() {
            return getMyBean();
        }
    }

    @Path("viewable")
    public static class AsViewableResource {
        @GET
        @Produces(MediaType.TEXT_HTML)
        public Viewable getAsHTML() {
            return new Viewable("index.testp", MODEL);
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public MyBean getAsJSON() {
            return getMyBean();
        }
    }

    /**
     * This test makes request for text/html which is preferred. The resource defines the method
     * {@link org.glassfish.jersey.tests.e2e.server.mvc.TemplateMethodSelectionTest.TemplateAnnotatedResourceMethod#getAsHTML()}
     * which returns {@link Map} for which there is not {@link javax.ws.rs.ext.MessageBodyWriter}. The absence of the
     * writer would cause that the method would not have been selected but as the {@link Template} annotation
     * is on the method, the {@link org.glassfish.jersey.server.internal.routing.MethodSelectingRouter} considers
     * it as if this would have been {@link Viewable} instead of the {@link Map}.
     */
    @Test
    public void testAnnotatedMethodByTemplateHtml() {
        final Response response = target().path("annotatedMethod").request("text/html;q=0.8", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        assertThat(response.readEntity(String.class),
                anyOf(containsString("{b=world, a=hello}"), containsString("{a=hello, b=world}")));
    }

    @Test
    public void testAnnotatedMethodByTemplateJson() {
        final Response response = target().path("annotatedMethod").request("text/html;q=0.6", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }

    @Test
    public void testAnnotatedClassByTemplateHtml() {
        final Response response = target().path("annotatedClass").request("text/html;q=0.8", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        assertTrue(response.readEntity(String.class).contains("model=This toString() method will be used to get model."));
    }

    @Test
    public void testAnnotatedClassByTemplateJson() {
        final Response response = target().path("annotatedClass").request("text/html;q=0.6", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }

    @Test
    public void testBasicHtml() {
        final Response response = target().path("basic").request("text/html;q=0.8", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        assertTrue(response.readEntity(String.class).contains("Hello World"));
    }

    @Test
    public void testBasicJson() {
        final Response response = target().path("basic").request("text/html;q=0.6", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }

    @Test
    public void testAsViewableHtml() {
        final Response response = target().path("viewable").request("text/html;q=0.8", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        assertThat(response.readEntity(String.class),
                anyOf(containsString("{b=world, a=hello}"), containsString("{a=hello, b=world}")));
    }

    @Test
    public void testAsViewableJson() {
        final Response response = target().path("viewable").request("text/html;q=0.6", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }

    /**
     * This test verifies that there is really no {@link javax.ws.rs.ext.MessageBodyWriter}
     * for {@code Map<String,String>}}. text/html is requested but application/json is chosen there is no
     * MBW for {@code Map}.
     */
    @Test
    public void testNoTemplateHtml() {
        final Response response = target().path("noTemplate").request("text/html;q=0.9", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }

    @Test
    public void testNoTemplateJson() {
        final Response response = target().path("noTemplate").request("text/html;q=0.6", "application/json;q=0.7").get();
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        assertEquals("hello", response.readEntity(MyBean.class).getName());
    }


}
