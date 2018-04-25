/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Covers JAX-RS defined @Context injectable values including
 * proxy support for these.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JaxRsInjectablesTest extends AbstractTest {

    private ContainerResponse resource(String uri) throws Exception {
        return apply(RequestContextBuilder.from(uri, "GET").build());
    }

    @Path("/")
    public static class PerRequestContextResource {

        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;

        @GET
        public String get() {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    public static class PerRequestContextConstructorParameterResource {

        public PerRequestContextConstructorParameterResource(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Path("/")
    public static class PerRequestContextMethodParameterResource {

        @GET
        public String get(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonContextResource {

        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;

        @GET
        public String get() {
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonContextConstructorParameterResource {

        public SingletonContextConstructorParameterResource(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            assertNotNull(sc);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Test
    public void testPerRequestInjected() throws Exception {
        initiateWebApplication(PerRequestContextResource.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestConstructorParameterInjected() throws Exception {
        initiateWebApplication(PerRequestContextConstructorParameterResource.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestMethodParameterInjected() throws Exception {
        initiateWebApplication(PerRequestContextMethodParameterResource.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testSingletonInjected() throws Exception {
        initiateWebApplication(SingletonContextResource.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testSingletonConstructorParameterInjected() throws Exception {
        initiateWebApplication(SingletonContextConstructorParameterResource.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Path("/")
    public static class StringWriterResource {

        @GET
        public String get() {
            return "GET";
        }
    }

    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterField implements MessageBodyWriter<String> {

        public StringWriterField() {
            int i = 0;
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
            arg6.write(arg0.getBytes());
        }

        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;
    }

    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterConstructor implements MessageBodyWriter<String> {

        public StringWriterConstructor(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc
        ) {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterMutlipleConstructor implements MessageBodyWriter<String> {

        public StringWriterMutlipleConstructor(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc
        ) {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
        }

        public StringWriterMutlipleConstructor() {
            fail();
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterMutliplePartialConstructor implements MessageBodyWriter<String> {

        public StringWriterMutliplePartialConstructor(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc
        ) {
            assertNotNull(ui);
            assertNotNull(hs);
            assertNotNull(r);
            assertNotNull(sc);
        }

        public StringWriterMutliplePartialConstructor(
                String rc,
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r) {
            fail();
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Test
    public void testProviderField() throws Exception {
        initiateWebApplication(StringWriterResource.class, StringWriterField.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderInstanceField() throws Exception {
        ResourceConfig rc = new ResourceConfig(StringWriterResource.class);
        rc.register(new StringWriterField());
        initiateWebApplication(rc);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderConstructor() throws Exception {
        initiateWebApplication(StringWriterResource.class, StringWriterConstructor.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderMultipleConstructor() throws Exception {
        initiateWebApplication(StringWriterResource.class, StringWriterMutlipleConstructor.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderMultiplePartialConstructor() throws Exception {
        initiateWebApplication(StringWriterResource.class, StringWriterMutliplePartialConstructor.class);

        assertEquals("GET", resource("/").getEntity());
    }

    @Path("/{p}")
    public static class PerRequestFieldResource {

        @PathParam("p")
        String p;

        @QueryParam("q")
        String q;

        @GET
        public String get() {
            return p + q;
        }
    }

    @Test
    public void testPerRequestParamInjected() throws Exception {
        initiateWebApplication(PerRequestFieldResource.class);

        assertEquals("foobar", resource("/foo?q=bar").getEntity());
    }

    @Provider
    @Produces("text/plain")
    public static class StringWriterParamConstructor implements MessageBodyWriter<String> {

        static int illegalStateExceptionCount = 0;
        static int runtimeExceptionCount = 0;

        public StringWriterParamConstructor(
                @Context UriInfo ui,
                @Context HttpHeaders hs,
                @Context Request r,
                @Context SecurityContext sc
        ) {

            try {
                ui.getAbsolutePath();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }

            try {
                hs.getDate();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }

            try {
                r.getMethod();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }

            try {
                sc.getAuthenticationScheme();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream os) throws IOException, WebApplicationException {
            String s = illegalStateExceptionCount + " " + runtimeExceptionCount + " " + arg0;
            os.write(s.getBytes());
        }
    }

    @Path("/")
    public static class StringWriterParamConstructorResource {

        @GET
        @Produces("text/plain")
        public String get() {
            return StringWriterParamConstructor.illegalStateExceptionCount + " "
                    + StringWriterParamConstructor.runtimeExceptionCount + " GET";
        }
    }

    @Test
    public void testStringWriterParamConstructor() throws Exception {
        initiateWebApplication(StringWriterParamConstructorResource.class, StringWriterParamConstructor.class);

        assertEquals("4 0 GET", resource("/").getEntity());
    }
}
