/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test-suite ensuring the correct functionality of name binding.
 *
 * @author Miroslav Fuksa
 * @author Michal Gajdos
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NameBindingTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, FooResource.class, BarResource.class, FooBarResource.class, FooFilter.class,
                BarFilter.class, FooBarFilter.class, PreMatchingFooFilter.class);
    }

    @Path("resource")
    public static class Resource {

        @GET
        public String noBinding() {
            return "noBinding";
        }

        @GET
        @FooBinding
        @Path("foo")
        public String foo() {
            return "foo";
        }

        @GET
        @BarBinding
        @Path("bar")
        public String bar() {
            return "bar";
        }

        @GET
        @FooBinding
        @BarBinding
        @Path("foobar")
        public String foobar() {
            return "foobar";
        }

        @GET
        @Path("preMatchingNameBinding")
        public String preMatchingNameBinding(@HeaderParam("header") @DefaultValue("bar") final String header) {
            return header;
        }
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface FooBinding {

    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface BarBinding {

    }

    @FooBinding
    public static class FooFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws
                IOException {
            responseContext.getHeaders().add(this.getClass().getSimpleName(), "called");
        }
    }

    @PreMatching
    @FooBinding
    public static class PreMatchingFooFilter implements ContainerRequestFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle("header", "foo");
        }
    }

    @BarBinding
    public static class BarFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws
                IOException {
            responseContext.getHeaders().add(this.getClass().getSimpleName(), "called");
        }
    }

    @FooBinding
    @BarBinding
    public static class FooBarFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws
                IOException {
            responseContext.getHeaders().add(this.getClass().getSimpleName(), "called");
        }
    }

    private static final Set<Class<?>> FILTERS = initialize();

    private static Set<Class<?>> initialize() {
        final Set<Class<?>> set = new HashSet<>();
        set.add(FooFilter.class);
        set.add(BarFilter.class);
        set.add(FooBarFilter.class);
        return set;
    }

    private void checkCalled(final Response response, final Class<?>... filtersThatShouldBeCalled) {
        final Set<Class<?>> positiveFilters = Arrays.stream(filtersThatShouldBeCalled).collect(Collectors.toSet());
        for (final Class<?> filter : FILTERS) {
            if (positiveFilters.contains(filter)) {
                assertEquals("called", response.getHeaders().getFirst(filter.getSimpleName()),
                        "Filter '" + filter.getSimpleName() + "' should be called.");
            } else {
                assertNull(response.getHeaders().get(filter.getSimpleName()),
                        "Filter '" + filter.getSimpleName() + "' should not be called.");
            }
        }
    }

    private Response _getResponse(final String path) {
        final Response response = target().path(path).request().get();
        assertEquals(200, response.getStatus());
        return response;
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testResourceNoBinding() {
        checkCalled(_getResponse("resource"));
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testResourceFooBinding() {
        checkCalled(_getResponse("resource/foo"), FooFilter.class);
    }

    /**
     * Reproducer for JERSEY-2739. Name bound annotation on a pre-matching filter should be ignored and the filter should be
     * invoked for each resource method (globally).
     */
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void preMatchingNameBinding() {
        final Response response = _getResponse("resource/preMatchingNameBinding");

        // Request filter - applied, even when the filter is name bound and the resource method is not.
        assertThat("Name binding on a @PreMatching filter not ignored.", response.readEntity(String.class), is("foo"));
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testResourceBarBinding() {
        checkCalled(_getResponse("resource/bar"), BarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testResourceFooBarBinding() {
        checkCalled(_getResponse("resource/foobar"), FooFilter.class, BarFilter.class, FooBarFilter.class);
    }

    @Path("foo-resource")
    @FooBinding
    public static class FooResource extends Resource {

    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooResourceNoBinding() {
        checkCalled(_getResponse("foo-resource"), FooFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooResourceFooBinding() {
        checkCalled(_getResponse("foo-resource/foo"), FooFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooResourceBarBinding() {
        checkCalled(_getResponse("foo-resource/bar"), FooFilter.class, BarFilter.class, FooBarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooResourceFooBarBinding() {
        checkCalled(_getResponse("foo-resource/foobar"), FooFilter.class, BarFilter.class, FooBarFilter.class);
    }

    @Path("bar-resource")
    @BarBinding
    public static class BarResource extends Resource {

    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testBarResourceNoBinding() {
        checkCalled(_getResponse("bar-resource"), BarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testBarResourceFooBinding() {
        checkCalled(_getResponse("bar-resource/foo"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testBarResourceBarBinding() {
        checkCalled(_getResponse("bar-resource/bar"), BarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testBarResourceFooBarBinding() {
        checkCalled(_getResponse("bar-resource/foobar"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

    @Path("foobar-resource")
    @BarBinding
    @FooBinding
    public static class FooBarResource extends Resource {

    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooBarResourceNoBinding() {
        checkCalled(_getResponse("foobar-resource"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooBarResourceFooBinding() {
        checkCalled(_getResponse("foobar-resource/foo"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooBarResourceBarBinding() {
        checkCalled(_getResponse("foobar-resource/bar"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    public void testFooBarResourceFooBarBinding() {
        checkCalled(_getResponse("foobar-resource/foobar"), BarFilter.class, FooFilter.class, FooBarFilter.class);
    }

}
