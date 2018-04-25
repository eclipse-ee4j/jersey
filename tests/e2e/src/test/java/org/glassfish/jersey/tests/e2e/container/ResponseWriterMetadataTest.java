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

package org.glassfish.jersey.tests.e2e.container;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michal Gajdos
 */
public class ResponseWriterMetadataTest extends JerseyContainerTest {

    public static class ValueHolder {

        private String value;

        public ValueHolder(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    @Provider
    @Produces("text/plain")
    public static class ValueHolderWriter implements MessageBodyWriter<ValueHolder> {

        public boolean isWriteable(final Class<?> c, final Type t, final Annotation[] as, final MediaType mediaType) {
            return ValueHolder.class == c;
        }

        public long getSize(final ValueHolder s, final Class<?> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType) {
            return -1;
        }

        public void writeTo(final ValueHolder s,
                            final Class<?> c,
                            final Type t,
                            final Annotation[] as,
                            final MediaType mt,
                            final MultivaluedMap<String, Object> headers,
                            final OutputStream out) throws IOException, WebApplicationException {

            headers.add("X-BEFORE-WRITE", "foo");
            out.write(s.value.getBytes());
            headers.add("X-AFTER-WRITE", "bar");
        }
    }

    @Path("/")
    public static class Resource {

        @GET
        public ValueHolder get() {
            return new ValueHolder("one");
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, ValueHolderWriter.class)
                .property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 1);
    }

    @Test
    public void testResponse() {
        final Response response = target().request().get();

        assertThat(response.readEntity(String.class), is("one"));
        assertThat(response.getHeaderString("X-BEFORE-WRITE"), is("foo"));

        if (factory instanceof InMemoryTestContainerFactory) {
            assertThat(response.getHeaderString("X-AFTER-WRITE"), is("bar"));
        } else {
            assertThat(response.getHeaderString("X-AFTER-WRITE"), nullValue());
        }
    }
}
