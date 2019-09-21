/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.inject.Inject;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link ExtendedUriInfo} e2e tests - testing e.g. getting matched resources, mapped throwable, etc.
 *
 * @author Michal Gajdos
 */
public class ExtendedUriInfoTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ThrowableResource.class, MappedThrowableResponseFilter.class, MappedExceptionMapper.class);
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface MappedThrowable {}

    @Path("mapped-throwable-test")
    @MappedThrowable
    public static class ThrowableResource {

        @GET
        @Path("unmapped")
        public Response unmapped() {
            throw new RuntimeException();
        }

        @GET
        @Path("mapped")
        public Response mapped() {
            throw new MappedException();
        }

        @GET
        @Path("webapp")
        public Response webapp() {
            throw new InternalServerErrorException();
        }

        @GET
        @Path("regular")
        public Response regular() {
            return Response.ok().build();
        }
    }

    public static class MappedException extends RuntimeException {
    }

    public static class MappedExceptionMapper implements ExceptionMapper<MappedException> {

        @Override
        public Response toResponse(final MappedException exception) {
            return Response.ok().build();
        }
    }

    @MappedThrowable
    public static class MappedThrowableResponseFilter implements ContainerResponseFilter {

        @Inject
        private ExtendedUriInfo uriInfo;

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
                throws IOException {
            responseContext.setEntity(Objects.toString(uriInfo.getMappedThrowable()));
        }
    }

    @Test
    public void testUnmappedThrowableValue() throws Exception {
        assertThat("Internal Server Error expected - response filter not invoked",
                target("mapped-throwable-test/unmapped").request().get().getStatus(),
                is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void testMappedThrowableValue() throws Exception {
        assertThat("MappedException expected in ExtendedUriInfo#getMappedThrowable",
                target("mapped-throwable-test/mapped").request().get().readEntity(String.class),
                is("org.glassfish.jersey.tests.e2e.server.ExtendedUriInfoTest$MappedException"));
    }

    @Test
    public void testWebAppThrowableValue() throws Exception {
        assertThat("InternalServerErrorException expected in ExtendedUriInfo#getMappedThrowable",
                target("mapped-throwable-test/webapp").request().get().readEntity(String.class),
                containsString("javax.ws.rs.InternalServerErrorException"));
    }

    @Test
    public void testRegularResourceValue() throws Exception {
        assertThat("null expected in ExtendedUriInfo#getMappedThrowable",
                target("mapped-throwable-test/regular").request().get().readEntity(String.class),
                is("null"));
    }
}
