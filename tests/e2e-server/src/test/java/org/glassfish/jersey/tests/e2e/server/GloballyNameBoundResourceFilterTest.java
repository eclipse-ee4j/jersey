/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * JAX-RS global name-bound filter tests.
 *
 * @author Marek Potociar
 * @see ResourceFilterTest
 */
public class GloballyNameBoundResourceFilterTest extends JerseyTest {

    public static final String TEST_REQUEST_HEADER = "test-request-header";

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @GloballyBound
    public static final class MyApplication extends ResourceConfig {
        public MyApplication() {
            super(
                    MyResource.class,
                    GloballyBoundRequestFilter.class,
                    GloballyBoundResponseFilter.class

            );
        }
    }

    @Test
    public void testGlobalyBoundPostMatching() {
        Response r = target("postMatching").request().get();
        assertThat(r.getStatus(), equalTo(200));
        assertThat(r.hasEntity(), is(true));
        assertThat(r.readEntity(String.class), equalTo("requestFilter-method-responseFilter"));
    }

    // See JERSEY-1554
    @Test
    public void testGlobalyBoundPostMatchingRequestFilterNotInvokedOn404() {
        Response r = target("notFound").request().get();
        assertEquals(404, r.getStatus());
        assertThat(r.hasEntity(), is(true));
        assertThat(r.readEntity(String.class), equalTo("responseFilter"));
    }

    @Path("/")
    public static class MyResource {

        @Path("postMatching")
        @GET
        public String getPostMatching(@Context HttpHeaders headers) {
            final String header = headers.getHeaderString(TEST_REQUEST_HEADER);
            return header + "-method";
        }
    }

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface GloballyBound {
    }

    @GloballyBound
    public static class GloballyBoundRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle(TEST_REQUEST_HEADER, "requestFilter");
        }
    }

    @GloballyBound
    public static class GloballyBoundResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            responseContext.setEntity(
                    responseContext.hasEntity() ? responseContext.getEntity() + "-responseFilter" : "responseFilter",
                    responseContext.getEntityAnnotations(),
                    responseContext.getMediaType());
        }
    }
}
