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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class EncodingTest extends JerseyTest {
    @Path("/")
    public static class EchoResource {
        @POST
        public String post(String text) {
            return text;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(
                EchoResource.class,
                org.glassfish.jersey.server.filter.EncodingFilter.class,
                GZipEncoder.class,
                DeflateEncoder.class
        );
    }

    @Test
    public void testGZip() throws IOException {
        test(new TestSpec() {
            @Override
            public InputStream decode(InputStream stream) throws IOException {
                return new GZIPInputStream(stream);
            }

            @Override
            public void checkHeadersAndStatus(Response response) {
                assertEquals("gzip", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }

            @Override
            public Invocation.Builder setHeaders(Invocation.Builder invBuilder) {
                return invBuilder.header(HttpHeaders.ACCEPT_ENCODING, "gzip");
            }
        });
    }

    @Test
    public void testDeflate() throws IOException {
        test(new TestSpec() {
            @Override
            public InputStream decode(InputStream stream) throws IOException {
                return new InflaterInputStream(stream);
            }

            @Override
            public void checkHeadersAndStatus(Response response) {
                assertEquals("deflate", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }

            @Override
            public Invocation.Builder setHeaders(Invocation.Builder invBuilder) {
                return invBuilder.header(HttpHeaders.ACCEPT_ENCODING, "deflate");
            }
        });
    }

    @Test
    public void testGZipPreferred() throws IOException {
        test(new TestSpec() {
            @Override
            public InputStream decode(InputStream stream) throws IOException {
                return new GZIPInputStream(stream);
            }

            @Override
            public void checkHeadersAndStatus(Response response) {
                assertEquals("x-gzip", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }

            @Override
            public Invocation.Builder setHeaders(Invocation.Builder invBuilder) {
                return invBuilder.header(HttpHeaders.ACCEPT_ENCODING, "deflate; q=.5, x-gzip");
            }
        });
    }

    @Test
    public void testGZipClientServer() throws IOException {
        test(new TestSpec() {
            @Override
            public WebTarget configure(WebTarget target) {
                target.register(GZipEncoder.class).register(EncodingFilter.class);
                return target;
            }

            @Override
            public void checkHeadersAndStatus(Response response) {
                assertEquals("gzip", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
        });
    }

    @Test
    public void testDeflatePreferredClientServer() throws IOException {
        test(new TestSpec() {
            @Override
            public Invocation.Builder setHeaders(Invocation.Builder invBuilder) {
                return invBuilder.header(HttpHeaders.ACCEPT_ENCODING, "deflate,gzip=.5");
            }

            @Override
            public WebTarget configure(WebTarget target) {
                target.register(DeflateEncoder.class)
                        .register(GZipEncoder.class).register(EncodingFilter.class);
                return target;
            }

            @Override
            public void checkHeadersAndStatus(Response response) {
                assertEquals("deflate", response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            }
        });
    }

    private void test(TestSpec testSpec) throws IOException {
        String input = "hello";
        Response response = testSpec.setHeaders(testSpec.configure(target()).request())
                .post(Entity.text(input));
        byte[] output = response.readEntity(byte[].class);
        testSpec.checkHeadersAndStatus(response);
        InputStream decoded = testSpec.decode(new ByteArrayInputStream(output));
        byte[] result = new byte[input.getBytes().length];
        int len = decoded.read(result);
        assertEquals(-1, decoded.read());
        decoded.close();
        assertEquals(input.getBytes().length, len);
        assertArrayEquals(input.getBytes(), result);
    }

    private static class TestSpec {
        public InputStream decode(InputStream stream) throws IOException {
            return stream;
        }

        public Invocation.Builder setHeaders(Invocation.Builder invBuilder) {
            return invBuilder;
        }

        public WebTarget configure(WebTarget target) {
            return target;
        }

        public void checkHeadersAndStatus(Response response) {
        }
    }
}
