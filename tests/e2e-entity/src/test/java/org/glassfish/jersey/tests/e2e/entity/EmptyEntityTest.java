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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JERSEY-1540.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class EmptyEntityTest extends AbstractTypeTester {

    public static class TestEmtpyBean {
        private final String value;

        public TestEmtpyBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Provider
    public static class TestEmptyBeanProvider implements MessageBodyReader<TestEmtpyBean>, MessageBodyWriter<TestEmtpyBean> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return TestEmtpyBean.class.isAssignableFrom(type);
        }

        @Override
        public TestEmtpyBean readFrom(Class<TestEmtpyBean> type,
                                      Type genericType,
                                      Annotation[] annotations,
                                      MediaType mediaType,
                                      MultivaluedMap<String, String> httpHeaders,
                                      InputStream entityStream) throws IOException, WebApplicationException {

            final String value = ReaderWriter.readFromAsString(entityStream, mediaType);
            return new TestEmtpyBean(value);
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return TestEmtpyBean.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(TestEmtpyBean testEmtpyBean,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(TestEmtpyBean testEmtpyBean,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            ReaderWriter.writeToAsString(testEmtpyBean.getValue(), entityStream, mediaType);
        }
    }

    @XmlRootElement
    public static class TestJaxbBean {

    }

    @Path("empty")
    public static class EmptyResource {

        @POST
        @Path("jaxbelem")
        public String jaxbelem(JAXBElement<String> jaxb) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("jaxbbean")
        public String jaxbbean(TestJaxbBean bean) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("jaxblist")
        public String jaxblist(List<TestJaxbBean> beans) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("boolean")
        public String bool(Boolean b) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("character")
        public String character(Character c) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("integer")
        public String integer(Integer i) {
            return "ERROR"; // shouldn't be called
        }

        @POST
        @Path("emptybean")
        public String emptyBean(TestEmtpyBean bean) {
            return (bean.getValue().isEmpty()) ? "PASSED" : "ERROR";
        }

        @GET
        @Path("getempty")
        public Response getEmpty(@Context HttpHeaders headers) {
            return Response.ok().type(headers.getAcceptableMediaTypes().get(0)).header(HttpHeaders.CONTENT_LENGTH, 0).build();
        }
    }

    public EmptyEntityTest() {
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testSendEmptyJAXBElement() {
        WebTarget target = target("empty/jaxbelem");
        final Response response = target.request().post(Entity.entity(null, MediaType.APPLICATION_XML_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyJAXBbean() {
        WebTarget target = target("empty/jaxbbean");
        final Response response = target.request().post(Entity.entity(null, MediaType.APPLICATION_XML_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyJAXBlist() {
        WebTarget target = target("empty/jaxblist");
        final Response response = target.request().post(Entity.entity(null, MediaType.APPLICATION_XML_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyBoolean() {
        WebTarget target = target("empty/boolean");
        final Response response = target.request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyCharacter() {
        WebTarget target = target("empty/character");
        final Response response = target.request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyInteger() {
        WebTarget target = target("empty/integer");
        final Response response = target.request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSendEmptyBean() {
        WebTarget target = target("empty/emptybean");
        final Response response = target.request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("PASSED", response.readEntity(String.class));
    }

    @Test
    public void testReceiveEmptyJAXBElement() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("application/xml").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(new GenericType<JAXBElement<String>>() {
            });
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("application/xml").get(new GenericType<JAXBElement<String>>() {
            });
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyJAXBbean() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("application/xml").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(TestJaxbBean.class);
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("application/xml").get(TestJaxbBean.class);
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyJAXBlist() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("application/xml").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(new GenericType<List<TestJaxbBean>>() {
            });
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("application/xml").get(new GenericType<List<TestJaxbBean>>() {
            });
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyBoolean() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("text/plain").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(Boolean.class);
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("text/plain").get(Boolean.class);
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyCharacter() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("text/plain").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(Character.class);
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("text/plain").get(Character.class);
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyInteger() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("text/plain").get();
        assertEquals(200, response.getStatus());

        try {
            response.readEntity(Integer.class);
            fail("ProcessingException expected.");
        } catch (ProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }

        try {
            target.request("text/plain").get(Integer.class);
            fail("ResponseProcessingException expected.");
        } catch (ResponseProcessingException ex) {
            assertSame(NoContentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testReceiveEmptyBean() {
        WebTarget target = target("empty/getempty");
        final Response response = target.request("text/plain").get();
        assertEquals(200, response.getStatus());

        assertTrue(response.readEntity(TestEmtpyBean.class).getValue().isEmpty());
        assertTrue(target.request("text/plain").get(TestEmtpyBean.class).getValue().isEmpty());
    }
}
