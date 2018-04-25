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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer for JERSEY-1845.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class InterceptedStreamCloseTest extends JerseyTest {
    @Path("resource")
    public static class TestResource {
        @POST
        public String post(String entity) {
            return entity;
        }
    }

    public static class ClientInterceptor implements ReaderInterceptor, WriterInterceptor {
        private volatile int readFromCount = 0;
        private volatile int inCloseCount = 0;
        private volatile int writeToCount = 0;
        private volatile int outCloseCount = 0;

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            readFromCount++;

            context.setInputStream(new FilterInputStream(context.getInputStream()) {
                @Override
                public void close() throws IOException {
                    inCloseCount++;
                    super.close();
                }
            });
            return context.proceed();
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            writeToCount++;

            context.setOutputStream(new FilterOutputStream(context.getOutputStream()) {
                @Override
                public void close() throws IOException {
                    outCloseCount++;
                    super.close();
                }
            });

            context.proceed();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testWrappedStreamClosed() {
        ClientInterceptor interceptor = new ClientInterceptor();

        final Response response = target().register(interceptor)
                .path("resource").request().post(Entity.entity("entity", MediaType.TEXT_PLAIN_TYPE));
        assertEquals("entity", response.readEntity(String.class));

        assertEquals(1, interceptor.readFromCount);
        assertEquals(1, interceptor.inCloseCount);
        assertEquals(1, interceptor.writeToCount);
        assertEquals(1, interceptor.outCloseCount);
    }
}
