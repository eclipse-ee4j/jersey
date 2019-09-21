/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michal Gajdos
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({MetaInfServicesTest.Enable.class, MetaInfServicesTest.DisableServer.class,
        MetaInfServicesTest.DisableClient.class})
public class MetaInfServicesTest {

    public static class MetaInf {

        private String value;

        public MetaInf(final String value) {
            this.value = value;
        }
    }

    @Path("resource")
    public static class MetaInfServicesResource {

        @POST
        public MetaInf post(final MetaInf entity) {
            return entity;
        }
    }

    public static class MessageProvider implements MessageBodyReader<MetaInf>, MessageBodyWriter<MetaInf> {

        @Context
        private Configuration config;

        @Override
        public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                  final MediaType mediaType) {
            return true;
        }

        @Override
        public MetaInf readFrom(final Class<MetaInf> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                               final InputStream entityStream) throws IOException, WebApplicationException {
            return new MetaInf(ReaderWriter.readFromAsString(entityStream, mediaType)
                    + "_read_" + config.getRuntimeType().name());
        }

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final MetaInf s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final MetaInf s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                            final OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write((s.value + "_write_" + config.getRuntimeType().name()).getBytes());
        }
    }

    public static class Enable extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(MetaInfServicesResource.class);
        }

        @Test
        public void testEnable() throws Exception {
            final Response response = target("resource").request().post(Entity.text(new MetaInf("foo")));

            assertThat(response.getStatus(), is(200));
            assertThat(response.readEntity(MetaInf.class).value,
                    is("foo_write_CLIENT_read_SERVER_write_SERVER_read_CLIENT"));
        }
    }

    public static class DisableServer extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(MetaInfServicesResource.class)
                    .property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        }

        @Test
        public void testDisableServer() throws Exception {
            final Response response = target("resource").request().post(Entity.text(new MetaInf("foo")));

            assertThat(response.getStatus(), is(200));
            assertThat(response.readEntity(MetaInf.class).value,
                    is("foo_write_CLIENT_read_SERVER_write_SERVER_read_CLIENT"));
        }
    }

    public static class DisableClient extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(MetaInfServicesResource.class);
        }

        @Override
        protected void configureClient(final ClientConfig config) {
            config.property(ClientProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        }

        @Test
        public void testDisableServer() throws Exception {
            final Response response = target("resource").request().post(Entity.text(new MetaInf("foo")));

            assertThat(response.getStatus(), is(200));
            assertThat(response.readEntity(MetaInf.class).value,
                    is("foo_write_CLIENT_read_SERVER_write_SERVER_read_CLIENT"));
        }
    }
}
