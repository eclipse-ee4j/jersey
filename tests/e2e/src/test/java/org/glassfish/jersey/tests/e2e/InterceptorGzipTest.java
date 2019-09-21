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

package org.glassfish.jersey.tests.e2e;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests gzip interceptors.
 *
 * @author Miroslav Fuksa
 *
 */
public class InterceptorGzipTest extends JerseyTest {

    private static final String FROM_RESOURCE = "-from_resource";

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(TestResource.class, GZIPWriterTestInterceptor.class,
                GZIPReaderTestInterceptor.class); // GZIPInterceptor.class
    }

    @Test
    public void testGzipInterceptorOnlyOnServer() throws IOException {
        client().register(GZIPWriterTestInterceptor.class);
        WebTarget target = target().path("test");
        String entity = "hello, this is text entity";
        Response response = target.request().put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
        InputStream is = response.readEntity(InputStream.class);
        GZIPInputStream gzipIs = new GZIPInputStream(is);
        BufferedReader br = new BufferedReader(new InputStreamReader(gzipIs));
        String str = br.readLine();
        assertEquals(entity + FROM_RESOURCE, str);
    }

    @Test
    public void testGzipInterceptorOnServerandClient() throws IOException {
        client().register(GZIPReaderTestInterceptor.class).register(GZIPWriterTestInterceptor.class);
        WebTarget target = target().path("test");
        String entity = "hello, this is text entity";
        Response response = target.request().put(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE));
        String str = response.readEntity(String.class);
        assertEquals(entity + FROM_RESOURCE, str);
    }

    @Path("test")
    public static class TestResource {

        @PUT
        @Produces("text/plain")
        @Consumes("text/plain")
        public String put(String str) {
            System.out.println("resource: " + str);
            return str + FROM_RESOURCE;
        }
    }

    @Provider
    @Priority(200)
    public static class CustomWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            final OutputStream old = context.getOutputStream();
            OutputStream newOut = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    if (b == Byte.MAX_VALUE) {
                        old.write(Byte.MIN_VALUE);
                    } else {
                        old.write(b + 1);
                    }
                }
            };
            context.setOutputStream(newOut);
            try {
                context.proceed();
            } finally {
                context.setOutputStream(old);
            }
        }
    }

    @Provider
    @Priority(200)
    public static class GZIPWriterTestInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            OutputStream old = context.getOutputStream();
            GZIPOutputStream newStream = new GZIPOutputStream(old);
            context.setOutputStream(newStream);
            if (context.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH)) {
                List<Object> clen = new ArrayList<>();
                clen.add(-1L);
                context.getHeaders().put(HttpHeaders.CONTENT_LENGTH, clen);
            }
            try {
                context.proceed();
            } finally {
                newStream.finish();
                context.setOutputStream(old);
            }
        }
    }

    @Provider
    @Priority(200)
    public static class GZIPReaderTestInterceptor implements ReaderInterceptor {

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            InputStream old = context.getInputStream();
            context.setInputStream(new GZIPInputStream(old));
            try {
                return context.proceed();
            } finally {
                context.setInputStream(old);
            }
        }

    }
}
