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

package org.glassfish.jersey.server.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.ContentEncoder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Martin Matula
 */
public class EncodingFilterTest {
    public static class FooEncoding extends ContentEncoder {
        public FooEncoding() {
            super("foo");
        }

        @Override
        public InputStream decode(String contentEncoding, InputStream encodedStream) throws IOException {
            return encodedStream;
        }

        @Override
        public OutputStream encode(String contentEncoding, OutputStream entityStream) throws IOException {
            return entityStream;
        }
    }

    @Test
    public void testNoInterceptor() {
        ResourceConfig rc = new ResourceConfig(EncodingFilter.class);
        ContainerResponseFilter filter = new ApplicationHandler(rc).getInjectionManager()
                .getInstance(ContainerResponseFilter.class);
        assertNotNull(filter);
        assertTrue(filter instanceof EncodingFilter);
        assertEquals(1, ((EncodingFilter) filter).getSupportedEncodings().size());
    }

    @Test
    public void testEnableFor() {
        EncodingFilter filter = initializeAndGetFilter();
        assertNotNull(filter);
        assertEquals(4, filter.getSupportedEncodings().size());
    }

    @Test
    public void testNoAcceptEncodingHeader() throws IOException {
        testEncoding(null);
    }

    @Test
    public void testAcceptEncodingHeaderNotSupported() throws IOException {
        testEncoding(null, "not-gzip");
    }

    @Test
    public void testGZipAcceptEncodingHeader() throws IOException {
        testEncoding("gzip", "gzip");
    }

    @Test
    public void testGZipPreferred() throws IOException {
        testEncoding("gzip", "foo; q=.5", "gzip");
    }

    @Test
    public void testFooPreferred() throws IOException {
        testEncoding("foo", "foo", "gzip; q=.5");
    }

    @Test
    public void testNotAcceptable() throws IOException {
        try {
            testEncoding(null, "one", "two", "*; q=0");
            fail(Response.Status.NOT_ACCEPTABLE + " response was expected.");
        } catch (WebApplicationException e) {
            assertEquals(Response.Status.NOT_ACCEPTABLE, e.getResponse().getStatusInfo());
        }
    }

    @Test
    public void testIdentityPreferred() throws IOException {
        testEncoding(null, "identity", "foo; q=.5");
    }

    @Test
    public void testAnyAcceptableExceptGZipAndIdentity() throws IOException {
        testEncoding("foo", "*", "gzip; q=0", "identity; q=0");
    }

    @Test
    public void testNoEntity() throws IOException {
        EncodingFilter filter = initializeAndGetFilter();
        ContainerRequest request = RequestContextBuilder.from("/resource", "GET").header(HttpHeaders.ACCEPT_ENCODING,
                "gzip").build();
        ContainerResponse response = new ContainerResponse(request, Response.ok().build());
        filter.filter(request, response);
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertNull(response.getHeaders().getFirst(HttpHeaders.VARY));
    }

    @SuppressWarnings("unchecked")
    private EncodingFilter initializeAndGetFilter() {
        ResourceConfig rc = new ResourceConfig();
        EncodingFilter.enableFor(rc, FooEncoding.class, GZipEncoder.class);
        return (EncodingFilter) new ApplicationHandler(rc).getInjectionManager().getInstance(ContainerResponseFilter.class);
    }

    private void testEncoding(String expected, String... accepted) throws IOException {
        EncodingFilter filter = initializeAndGetFilter();
        RequestContextBuilder builder = RequestContextBuilder.from("/resource", "GET");
        for (String a : accepted) {
            builder.header(HttpHeaders.ACCEPT_ENCODING, a);
        }
        ContainerRequest request = builder.build();
        ContainerResponse response = new ContainerResponse(request, Response.ok("OK!").build());
        filter.filter(request, response);
        if (response.getStatus() != 200) {
            throw new WebApplicationException(Response.status(response.getStatus()).build());
        }
        assertEquals(expected, response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaderString(HttpHeaders.VARY));
    }
}
