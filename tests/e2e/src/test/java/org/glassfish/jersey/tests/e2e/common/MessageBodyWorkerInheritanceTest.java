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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test-case for JERSEY-1481.
 *
 * {@link JacksonFeature Jackson provider} should not take precedence over our
 * custom provider.
 *
 * @author Michal Gajdos
 */
public class MessageBodyWorkerInheritanceTest extends JerseyTest {

    public static interface Model<T> {

        public T getValue();
    }

    public static class StringModel implements Model<String> {

        private final String value;

        public StringModel(final String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public static interface InterfaceType extends Model {
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Provider
    public static class ModelReader<T extends Model> implements MessageBodyReader<T> {

        @Override
        public boolean isReadable(
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {

            return Model.class.isAssignableFrom(type);
        }

        @Override
        public T readFrom(
                    Class<T> type,
                    Type genericType,
                    Annotation[] annotations,
                    MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders,
                    InputStream entityStream) throws IOException, WebApplicationException {

            return (T) new InterfaceType() {
                @Override
                public Object getValue() {
                    return "fromInterfaceTypeReader";
                }
            };
        }
    }

    @Provider
    public abstract static class BaseProvider<T> implements MessageBodyWriter<T> {

        @Override
        public boolean isWriteable(final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final T t,
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final T t,
                final Class<?> type,
                final Type genericType,
                final Annotation[] annotations,
                final MediaType mediaType,
                final MultivaluedMap<String, Object> httpHeaders,
                final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(getContent(t).getBytes("UTF-8"));
        }

        public abstract String getContent(T proxy);
    }

    @Provider
    @Produces(MediaType.APPLICATION_JSON)
    public static class GenericModelWriter extends BaseProvider<Model> {

        @Override
        public String getContent(final Model proxy) {
            return "{\"bar\":\"" + proxy.getValue() + "\"}";
        }
    }

    @Provider
    @Produces(MediaType.APPLICATION_JSON)
    public static class IntegerModelWriter extends BaseProvider<Model<Integer>> {

        @Override
        public String getContent(final Model<Integer> proxy) {
            return "{\"foo\":\"" + proxy.getValue() + "\"}";
        }
    }

    @Path("resource")
    public static class Resource {

        @GET
        public Model<String> getStringModel() {
            return new StringModel("foo");
        }

        @POST
        @Produces(MediaType.TEXT_PLAIN)
        public String post(InterfaceType t) {
            return t.getValue().toString();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .register(GenericModelWriter.class)
                .register(ModelReader.class)
                .register(JacksonFeature.class);
    }

    @Test
    public void testMessageBodyWorkerInheritance() throws Exception {
        assertEquals("{\"bar\":\"foo\"}", target().path("resource").request(MediaType.APPLICATION_JSON_TYPE).get(String.class));
    }

    @Test
    public void testMessageBodyWorkerInterfaceInheritance() throws Exception {

        final Response response = target().path("resource")
                                     .request().post(Entity.json("{\"value\":\"ignored\"}"));

        assertEquals(200, response.getStatus());
        assertEquals("fromInterfaceTypeReader", response.readEntity(String.class));
    }
}
