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
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that appropriate methods are called in the intercepted output stream.
 *
 * @author Miroslav Fuksa
 *
 */
public class StreamMethodCallTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(TestWriterInterceptor.class, TestResource.class);
    }

    public static class TestWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            final OutputStream outputStreamoldOs = context.getOutputStream();
            context.setOutputStream(new TestOutputStream(new GZIPOutputStream(outputStreamoldOs)));
            context.proceed();
        }
    }

    public static class TestOutputStream extends OutputStream {
        private final GZIPOutputStream gzos;
        public static boolean closeCalled = false;
        public static boolean flushCalledBeforeClose = false;
        public static boolean writeCalled = false;

        public TestOutputStream(GZIPOutputStream gzos) {
            this.gzos = gzos;
        }

        @Override
        public void write(int b) throws IOException {
            writeCalled = true;
            gzos.write(b);
        }

        @Override
        public void close() throws IOException {
            TestOutputStream.closeCalled = true;
            gzos.close();
        }

        @Override
        public void flush() throws IOException {
            if (!closeCalled) {
                flushCalledBeforeClose = true;
            }
            gzos.flush();
        }
    }

    @Path("resource")
    public static class TestResource {
        @GET
        public String get() {
            return "get";
        }
    }

    @Test
    public void testCalledMethods() {
        final Response response = target().path("resource").request().get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue("close() has not been called.", TestOutputStream.closeCalled);
        Assert.assertTrue("flush() has not been called before close().", TestOutputStream.flushCalledBeforeClose);
        Assert.assertTrue("write() has not been called.", TestOutputStream.writeCalled);
    }
}
