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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer for JERSEY-2541.
 *
 * Make sure that type parameter will be retained for resource method
 * of a sub-resource locator that returns a dynamic proxy.
 * This test should cover also the EJB case, as the common cause
 * is missing type parameter.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class SubResourceDynamicProxyTest extends JerseyTest {

    @Override
    public Application configure() {
        return new ResourceConfig(ListProvider.class, RootResource.class);
    }

    /**
     * Sub-resource interface so that it is easy to make a dynamic proxy
     * with the standard Java reflection API.
     */
    public static interface SubResource {

        @GET
        public List<Foo> getGreeting();
    }

    /**
     * Helper type to be used as a type parameter.
     */
    public static class Foo {
    }

    /**
     * Message body writer that uses to check presence of type parameter in the provided entity type.
     * If no type parameter is found the provider refuses to process corresponding entity,
     * which would lead to an error.
     */
    @Provider
    public static class ListProvider implements MessageBodyWriter<List<Foo>> {

        /**
         * This is the data that would be written to the response body by the {@link ListProvider} bellow.
         */
        static final String CHECK_DATA = "ensure this one makes it to the client";

        /**
         * We need to work with a non-null entity here so that the worker could do it's job.
         */
        static final LinkedList<Foo> TEST_ENTITY = new LinkedList<Foo>() {
            {
                add(new Foo());
            }
        };

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            if (!(genericType instanceof ParameterizedType)) {
                return false;
            }

            final ParameterizedType pt = (ParameterizedType) genericType;

            if (pt.getActualTypeArguments().length > 1) {
                return false;
            }

            if (!(pt.getActualTypeArguments()[0] instanceof Class)) {
                return false;
            }

            final Class listClass = (Class) pt.getActualTypeArguments()[0];
            return listClass == Foo.class;
        }

        @Override
        public long getSize(List<Foo> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(List<Foo> t, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {

            assertEquals(t, TEST_ENTITY);
            entityStream.write(CHECK_DATA.getBytes());
        }
    }

    @Path("root")
    public static class RootResource {

        /**
         * Sub-resource locator is used here, so that resource model will be built
         * at runtime, when the actual handler is the dynamic proxy.
         *
         * @return dynamic proxy for the sub-resource
         */
        @Path("sub")
        public SubResource getSubresource() {
            return (SubResource) Proxy.newProxyInstance(
                    RootResource.class.getClassLoader(),
                    new Class<?>[] {SubResource.class},
                    new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            return ListProvider.TEST_ENTITY;
                        }
                    });
        }
    }

    /**
     * Make sure the request is processed without errors, and the data
     * written by the {@link ListProvider} is returned back to the client.
     */
    @Test
    public void testSubResourceProxy() {

        final Response response = target("/root/sub").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(ListProvider.CHECK_DATA, response.readEntity(String.class));
    }
}
