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

package org.glassfish.jersey.server.internal.routing;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.RuntimeResource;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * {@link ExtendedUriInfo} unit tests - testing e.g. getting matched resources, mapped throwable, etc.
 *
 * @author Michal Gajdos
 * @author Miroslav Fuksa
 */
public class ExtendedUriInfoTest {

    private ApplicationHandler getApplication() {
        return new ApplicationHandler(new ResourceConfig(SimpleResource.class, ResourceWithLocator.class));
    }

    @Path("root")
    public static class SimpleResource {

        @Inject
        private ExtendedUriInfo extendedUriInfo;

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            assertEquals("root", extendedUriInfo.getMatchedModelResource().getPath());
            assertEquals(MediaType.TEXT_PLAIN_TYPE, extendedUriInfo.getMatchedResourceMethod().getProducedTypes().get(0));
            assertEquals("/root", extendedUriInfo.getMatchedRuntimeResources().get(0).getRegex());
            assertEquals(1, extendedUriInfo.getMatchedRuntimeResources().size());

            return "root";
        }

        @Path("child")
        @GET
        public String child() {
            final Resource resource = extendedUriInfo.getMatchedModelResource();
            assertEquals("child", resource.getPath());
            final List<RuntimeResource> runtimeResources = extendedUriInfo.getMatchedRuntimeResources();
            assertEquals("root", resource.getParent().getPath());
            assertEquals(2, runtimeResources.size());
            assertEquals("/child;/root", convertToString(runtimeResources));
            assertEquals("/child", runtimeResources.get(0).getRegex());
            assertEquals("/root", runtimeResources.get(1).getRegex());
            return "child";
        }
    }

    @Test
    public void testResourceMethod() throws ExecutionException, InterruptedException {
        final ApplicationHandler applicationHandler = getApplication();
        final ContainerResponse response = applicationHandler.apply(RequestContextBuilder.from("/root", "GET").build()).get();
        assertEquals("root", response.getEntity());
    }

    @Test
    public void testChildResourceMethod() throws ExecutionException, InterruptedException {
        final ApplicationHandler applicationHandler = getApplication();
        final ContainerResponse response = applicationHandler.apply(RequestContextBuilder.from("/root/child", "GET").build())
                .get();
        assertEquals("child", response.getEntity());
    }

    @Path("locator-test")
    public static class ResourceWithLocator {

        @Path("/")
        public Class<SubResourceLocator> getResourceLocator() {
            return SubResourceLocator.class;
        }

        @Path("sublocator")
        public Class<SubResourceLocator> getSubResourceLocator() {
            return SubResourceLocator.class;
        }

    }

    public static class SubResourceLocator {

        @Inject
        private ExtendedUriInfo extendedUriInfo;

        @GET
        public String get() {
            assertFalse(extendedUriInfo.getMatchedModelResource().getPath() != null);
            final List<RuntimeResource> matchedRuntimeResources = extendedUriInfo.getMatchedRuntimeResources();
            return convertToString(matchedRuntimeResources);
        }

        @GET
        @Path("subget")
        public String subGet() {
            final List<RuntimeResource> matchedRuntimeResources = extendedUriInfo.getMatchedRuntimeResources();
            assertEquals("/subget", matchedRuntimeResources.get(0).getRegex());
            assertEquals("subget", extendedUriInfo.getMatchedModelResource().getPath());
            return convertToString(matchedRuntimeResources);
        }

        @Path("sub")
        public Class<SubResourceLocator> getRecursive() {
            final List<RuntimeResource> matchedRuntimeResources = extendedUriInfo.getMatchedRuntimeResources();
            assertEquals("/sub", matchedRuntimeResources.get(0).getRegex());
            assertNull(extendedUriInfo.getMatchedModelResource());
            assertNull(extendedUriInfo.getMatchedResourceMethod());
            return SubResourceLocator.class;
        }
    }

    private static String convertToString(final List<RuntimeResource> matchedResources) {
        final StringBuilder sb = new StringBuilder();
        for (final RuntimeResource resource : matchedResources) {
            final String path = resource.getRegex();
            sb.append(path == null ? "<no-path>" : path);
            sb.append(";");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Test
    public void testLocator() throws ExecutionException, InterruptedException {
        final String requestUri = "/locator-test";
        final String expectedResourceList = "<no-path>;/locator\\-test";

        _testResourceList(requestUri, expectedResourceList);
    }

    private void _testResourceList(final String requestUri, final String expectedResourceList) throws InterruptedException,
            ExecutionException {
        final ApplicationHandler applicationHandler = getApplication();
        final ContainerResponse response = applicationHandler.apply(RequestContextBuilder.from(requestUri, "GET").build()).get();
        assertEquals(200, response.getStatus());
        final String resources = (String) response.getEntity();
        assertEquals(expectedResourceList, resources);
    }

    @Test
    public void testLocatorChild() throws ExecutionException, InterruptedException {
        _testResourceList("/locator-test/subget", "/subget;<no-path>;/locator\\-test");
    }

    @Test
    public void testSubResourceLocator() throws ExecutionException, InterruptedException {
        _testResourceList("/locator-test/sublocator", "<no-path>;/sublocator;/locator\\-test");
    }

    @Test
    public void testSubResourceLocatorSubGet() throws ExecutionException, InterruptedException {
        _testResourceList("/locator-test/sublocator/subget", "/subget;<no-path>;/sublocator;/locator\\-test");
    }

    @Test
    public void testSubResourceLocatorRecursive() throws ExecutionException, InterruptedException {
        _testResourceList("/locator-test/sublocator/sub", "<no-path>;/sub;<no-path>;/sublocator;/locator\\-test");
    }

    @Test
    public void testSubResourceLocatorRecursive2() throws ExecutionException, InterruptedException {
        _testResourceList("/locator-test/sublocator/sub/sub/subget",
                "/subget;<no-path>;/sub;<no-path>;/sub;<no-path>;/sublocator;/locator\\-test");
    }
}
