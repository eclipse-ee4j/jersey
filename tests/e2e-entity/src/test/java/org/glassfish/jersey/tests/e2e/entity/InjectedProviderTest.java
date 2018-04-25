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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.client.ClientConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 */
public class InjectedProviderTest extends AbstractTypeTester {

    public static class Bean implements Serializable {

        private String string;

        public Bean() {
        }

        public Bean(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }

    @Provider
    public static class BeanReader implements MessageBodyReader<Bean> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == Bean.class;
        }

        @Override
        public Bean readFrom(
                Class<Bean> type,
                Type genericType,
                Annotation annotations[],
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            ObjectInputStream oin = new ObjectInputStream(entityStream);
            try {
                return (Bean) oin.readObject();
            } catch (ClassNotFoundException cause) {
                throw new IOException(cause);
            }
        }
    }

    @Provider
    public static class InjectedBeanReaderWriter extends BeanReader implements MessageBodyWriter<Bean> {

        @Context
        UriInfo uriInfo;

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == Bean.class;
        }

        @Override
        public void writeTo(
                Bean t,
                Class<?> type,
                Type genericType,
                Annotation annotations[],
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            t.setString(uriInfo.getRequestUri().toString());
            ObjectOutputStream out = new ObjectOutputStream(entityStream);
            out.writeObject(t);
            out.flush();
        }

        @Override
        public long getSize(Bean t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }
    }

    @Path("/one/two/{id}")
    public static class BeanResource {

        @GET
        public Bean get() {
            return new Bean("");
        }
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(BeanReader.class);
    }

    @Test
    public void testBean() throws Exception {
        Bean bean3 = target("one/two/three").request().get(Bean.class);
        Bean bean4 = target("one/two/four").request().get(Bean.class);

        final Map<String, String> map3 = new HashMap<String, String>() {{
            put("id", "three");
        }};
        final Map<String, String> map4 = new HashMap<String, String>() {{
            put("id", "four");
        }};

        String requestUri3 = target().getUriBuilder()
                .path(BeanResource.class).buildFromMap(map3).toString();
        String requestUri4 = target().getUriBuilder()
                .path(BeanResource.class).buildFromMap(map4).toString();

        assertEquals(requestUri3, bean3.getString());
        assertEquals(requestUri4, bean4.getString());
    }
}
