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
import java.util.zip.GZIPInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class GzipContentEncodingTest extends JerseyContainerTest {

    @Path("/")
    public static class Resource {

        @GET
        public String get() {
            return "GET";
        }

        @POST
        public String post(final String content) {
            return content;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, EncodingFilter.class, GZipEncoder.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(new ReaderInterceptor() {
            @Override
            public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
                context.setInputStream(new GZIPInputStream(context.getInputStream()));
                return context.proceed();
            }
        });
    }

    @Test
    public void testGet() {
        final Response response = target().request()
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(response.readEntity(String.class), is("GET"));
    }

    @Test
    public void testPost() {
        final Response response = target().request()
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .post(Entity.text("POST"));

        assertThat(response.readEntity(String.class), is("POST"));
    }
}
