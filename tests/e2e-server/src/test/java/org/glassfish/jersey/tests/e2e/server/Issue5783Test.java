/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

public class Issue5783Test extends JerseyTest {

    private static final String ERROR = "Intentional issue5783 exception";
    private static volatile String exceptionMessage;

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, ResponseFilter.class);
    }

    @Test
    public void closeException() throws InterruptedException {
        target("/test").request().get();
        assertEquals(ERROR, exceptionMessage);
    }

    @Path("/test")
    public static class Resource {

        @GET
        public Response closeException(@Context ContainerRequest request) {
            // Save the exception when response.getRequestContext().getResponseWriter().failure(e)
            ContainerResponseWriter writer = request.getResponseWriter();
            ContainerResponseWriter proxy = (ContainerResponseWriter) Proxy.newProxyInstance(
                    ContainerResponseWriter.class.getClassLoader(),
                    new Class<?>[]{ContainerResponseWriter.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("failure".equals(method.getName())) {
                        exceptionMessage = ((Throwable) args[0]).getCause().getMessage();
                    }
                    return method.invoke(writer, args);
                }
            });
            request.setWriter(proxy);
            return Response.ok().build();
        }
    }

    @Provider
    public static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                throws IOException {
            // Hack it to make ContainerResponse#close throws one exception
            responseContext.setEntity("something");
            responseContext.setEntityStream(new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    throw new IOException(ERROR);
                }
            });
        }
    }
}
