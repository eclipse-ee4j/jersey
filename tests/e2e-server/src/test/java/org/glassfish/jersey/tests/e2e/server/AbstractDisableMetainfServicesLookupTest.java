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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.inject.Singleton;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;

/**
 * This is base for tests testing enabling/disabling configuration property
 * {@link org.glassfish.jersey.server.ServerProperties#METAINF_SERVICES_LOOKUP_DISABLE}.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public abstract class AbstractDisableMetainfServicesLookupTest extends JerseyTest {

    protected void testGet(int expectedGetResponseCode, int expectedPostResponseCode) throws Exception {
        final String name = "Jersey";
        {
            Response response = target("/").path(name).request().get();
            Assert.assertEquals(expectedGetResponseCode, response.getStatus());

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                UselessMessage entity = response.readEntity(UselessMessage.class);
                if (entity != null) {
                    Assert.assertEquals("Hello " + name, entity.getMessage());
                }
            }
        }
        {
            Entity<UselessMessage> uselessMessageEntity = Entity.entity(new UselessMessage(name), MediaType.TEXT_PLAIN_TYPE);
            Response response = target("/").request().post(uselessMessageEntity);
            Assert.assertEquals(expectedPostResponseCode, response.getStatus());

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                String entity = response.readEntity(String.class);
                if (entity.length() > 0) {
                    Assert.assertEquals(name, entity);
                }
            }
        }
    }


    @Override
    protected Application configure() {
        final ResourceConfig config = new ResourceConfig(Resource.class);
        config.register(new MetainfServicesBinder(config));
        return config;
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(new MetainfServicesBinder(config));
    }

    @Path("/")
    @Produces("text/plain")
    @Consumes("text/plain")
    public static class Resource {

        @GET
        @Path("{name}")
        public UselessMessage get(@PathParam("name") final String name) {
            UselessMessage result = new UselessMessage();
            result.setMessage("Hello " + name);
            return result;
        }

        @POST
        public String post(final UselessMessage message) {
            return message.getMessage();
        }

    } // class Resource


    /**
     * META-INF/services/javax.ws.rs.ext.MessageBodyReader OR META-INF/services/javax.ws.rs.ext.MessageBodyWriter :
     * org.glassfish.jersey.tests.e2e.server.AbstractDisableMetainfServicesLookupTest$UselessMessageBodyWriter
     */
    @Produces("text/plain")
    @Consumes("text/plain")
    @Singleton
    public static class UselessMessageProvider extends AbstractMessageReaderWriterProvider<UselessMessage> {

        public UselessMessageProvider() {
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == UselessMessage.class;
        }

        @Override
        public UselessMessage readFrom(
                Class<UselessMessage> type,
                Type genericType,
                Annotation annotations[],
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return new UselessMessage(readFromAsString(entityStream, mediaType));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == UselessMessage.class;
        }

        @Override
        public long getSize(UselessMessage s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return s.getMessage().length();
        }

        @Override
        public void writeTo(
                UselessMessage t,
                Class<?> type,
                Type genericType,
                Annotation annotations[],
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            writeToAsString(t.getMessage(), entityStream, mediaType);
        }
    } // class UselessMessageBodyWriter


    public static class UselessMessage {

        private String message;

        public UselessMessage() {
        }

        public UselessMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "UselessMessage{"
                    + "message='" + message + '\''
                    + '}';
        }
    } // class UselessMessage

    private static class MetainfServicesBinder extends AbstractBinder {

        private final Map<String, Object> properties;
        private final RuntimeType runtimeType;

        public MetainfServicesBinder(final Configuration config) {
            this.properties = config.getProperties();
            this.runtimeType = config.getRuntimeType();
        }

        @Override
        protected void configure() {
            // Message Body providers.
            install(new ServiceFinderBinder<>(MessageBodyReader.class, properties, runtimeType));
            install(new ServiceFinderBinder<>(MessageBodyWriter.class, properties, runtimeType));
            // Exception Mappers.
            install(new ServiceFinderBinder<>(ExceptionMapper.class, properties, runtimeType));
        }
    }
}
