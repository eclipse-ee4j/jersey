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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class HeadTest extends JerseyContainerTest {

    private static final List<TestContainerFactory> FACTORIES = Arrays.asList(
            new GrizzlyTestContainerFactory(),
            new InMemoryTestContainerFactory(),
            new SimpleTestContainerFactory(),
            new JettyTestContainerFactory());

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestContainerFactory[]> parameters() throws Exception {
        return FACTORIES.stream().map(input -> new TestContainerFactory[]{input}).collect(Collectors.toList());
    }

    @Path("/")
    public static class Resource {

        @Path("string")
        @GET
        public String getString() {
            return "GET";
        }

        @Path("byte")
        @GET
        public byte[] getByte() {
            return "GET".getBytes();
        }

        @Path("ByteArrayInputStream")
        @GET
        public InputStream getInputStream() {
            return new ByteArrayInputStream("GET".getBytes());
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testHeadString() {
        _testHead("string", MediaType.TEXT_PLAIN_TYPE);
    }

    @Test
    public void testHeadByte() {
        _testHead("byte", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Test
    public void testHeadByteArrayInputStream() {
        _testHead("ByteArrayInputStream", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    private void _testHead(final String path, final MediaType mediaType) {
        final Response response = target(path).request(mediaType).head();
        assertThat(response.getStatus(), is(200));

        final String lengthStr = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
        assertThat(lengthStr, notNullValue());
        assertThat(Integer.parseInt(lengthStr), is(3));
        assertThat(response.getMediaType(), is(mediaType));
        assertFalse(response.hasEntity());
    }
}
