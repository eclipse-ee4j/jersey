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

package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.glassfish.jersey.uri.internal.UriTemplateParser;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test {@link ResourceModel} and {@link RuntimeResourceModel}.
 *
 * @author Miroslav Fuksa
 */
public class ResourceModelTest {

    @Path("a")
    public static class SimpleResourceA {
        @GET
        public String get() {
            return "get";
        }

        @GET
        @Path("child")
        public String getChild() {
            return "get";
        }

        @Path("{child-template-x}")
        @GET
        public String getChildTemplateX() {
            return "get";
        }

        @Path("{child-template-y}")
        @POST
        public String postChildTemplateY(String entity) {
            return "get";
        }
    }

    @Path("a")
    public static class SimpleResourceB {
        @POST
        public String post(String entity) {
            return "post";
        }
    }

    @Path("{a}")
    public static class ResourceTemplateA {
        @GET
        public String get() {
            return "get";
        }

        @PUT
        public String put(String entity) {
            return "put";
        }


        @Path("{a-child-template-q}")
        @GET
        public String getChildTemplateQ() {
            return "get";
        }

        @Path("{a-child-template-w}")
        @POST
        public String getChildTemplateW(String entity) {
            return "get";
        }
    }

    @Path("{b}")
    public static class ResourceTemplateB {
        @POST
        public String post() {
            return "post";
        }

        @Path("{a-child-template-q}")
        @DELETE
        public String getChildTemplateQ(String entity) {
            return "get";
        }

        @Path("{b-child-template-z}")
        @PUT
        public String getChildTemplateW(String entity) {
            return "get";
        }

        @Path("another-child")
        @PUT
        public String putChildAnother(String entity) {
            return "put";
        }
    }

    @Test
    public void testResourceModel() {
        ResourceModel resourceModel = getResourceModel();
        final List<Resource> rootResources = resourceModel.getRootResources();
        assertEquals(3, rootResources.size());
        assertEquals(3, resourceModel.getResources().size());
        final Resource resourceA = ResourceTestUtils.getResource(getResourceModel().getRootResources(), "a");
        ResourceTestUtils.containsExactMethods(resourceA, false, "GET", "POST");
        final List<Resource> childResources = resourceA.getChildResources();
        assertEquals(3, childResources.size());
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(childResources, "{child-template-x}"), false, "GET");
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(childResources, "{child-template-y}"), false,
                "POST");

        final Resource templateA = ResourceTestUtils.getResource(getResourceModel().getRootResources(), "{a}");
        ResourceTestUtils.containsExactMethods(templateA, false, "GET", "PUT");
        final List<Resource> templateAChildResources = templateA.getChildResources();
        assertEquals(2, templateAChildResources.size());
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(templateAChildResources,
                "{a-child-template-q}"), false, "GET");
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(templateAChildResources,
                "{a-child-template-w}"), false, "POST");

        final Resource templateB = ResourceTestUtils.getResource(getResourceModel().getRootResources(), "{b}");
        ResourceTestUtils.containsExactMethods(templateB, false, "POST");
        final List<Resource> templateBChildResources = templateB.getChildResources();
        assertEquals(3, templateBChildResources.size());
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(templateBChildResources,
                "{a-child-template-q}"), false, "DELETE");
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(templateBChildResources,
                "{b-child-template-z}"), false, "PUT");
        ResourceTestUtils.containsExactMethods(ResourceTestUtils.getResource(templateBChildResources,
                "another-child"), false, "PUT");
    }

    /**
     * Reproducer for JERSEY-2946: StackOverFlowError in Resource.toString()
     */
    @Test
    public void testResourceWithChildrenDoesNotOverflowToString() {
        final Resource.Builder parentBuilder = Resource.builder("parent");
        final Resource.Builder childBuilder = parentBuilder.addChildResource("child");
        final Resource.Builder locatorBuilder = parentBuilder.addChildResource("child");

        // In Jersey 2.21 this throws StackOverFlowError.
        assertNotNull(childBuilder.toString());
        assertNotNull(parentBuilder.toString());
    }

    @Test
    public void testRuntimeResourceModel() {
        ResourceModel resourceModel = getResourceModel();
        final RuntimeResourceModel runtimeResourceModel = resourceModel.getRuntimeResourceModel();
        final List<RuntimeResource> runtimeResources = runtimeResourceModel.getRuntimeResources();
        assertEquals(2, runtimeResources.size());
        final RuntimeResource a = ResourceTestUtils.getRuntimeResource(runtimeResources, "/a");
        ResourceTestUtils.containsExactMethods(a, false, "GET", "POST");
        final List<RuntimeResource> aChildRuntimeResources = a.getChildRuntimeResources();
        assertEquals(2, aChildRuntimeResources.size());

        final RuntimeResource childResource = ResourceTestUtils.getRuntimeResource(aChildRuntimeResources, "/child");
        ResourceTestUtils.containsExactMethods(childResource, false,

                "GET");


        testTemplate(runtimeResources);
    }

    public static Resource getParentResource(RuntimeResource runtimeResource, String path) {
        int i = 0;

        for (Resource resource : runtimeResource.getResources()) {
            if (path.equals(resource.getPath())) {
                return runtimeResource.getParentResources().get(i);
            }
            i++;
        }
        fail("Resource " + path + " not found");
        return null;
    }

    private void testTemplate(List<RuntimeResource> runtimeResources) {
        final String regexTemplate = "/(" + UriTemplateParser.TEMPLATE_VALUE_PATTERN.pattern() + ")";
        final RuntimeResource template = ResourceTestUtils.getRuntimeResource(runtimeResources, regexTemplate);
        ResourceTestUtils.containsExactMethods(template, false, "GET", "PUT", "POST");
        final List<RuntimeResource> templateChildResources = template.getChildRuntimeResources();
        assertEquals(2, templateChildResources.size());

        final RuntimeResource templateChild = ResourceTestUtils.getRuntimeResource(templateChildResources, regexTemplate);
        ResourceTestUtils.containsExactMethods(templateChild, false, "GET", "POST", "PUT", "DELETE");

        getParentResource(templateChild, "{a-child-template-q}").getPath().equals("{a}");
        getParentResource(templateChild, "{a-child-template-w}").getPath().equals("{a}");

        getParentResource(templateChild, "{a-child-template-q}").getPath().equals("{b}");
        getParentResource(templateChild, "{b-child-template-z}").getPath().equals("{b}");


        final RuntimeResource child = ResourceTestUtils.getRuntimeResource(templateChildResources, "/another\\-child");
        getParentResource(child, "another-child").getPath().equals("{b}");
        ResourceTestUtils.containsExactMethods(child, false, "PUT");
    }

    private ResourceModel getResourceModel() {
        final List<Resource> resources = new ArrayList<>();
        resources.add(Resource.from(SimpleResourceA.class));
        resources.add(Resource.from(SimpleResourceB.class));
        resources.add(Resource.from(ResourceTemplateA.class));
        resources.add(Resource.from(ResourceTemplateB.class));

        return new ResourceModel.Builder(resources, false).build();
    }


    public static class NonRootResourceA {
        @GET
        public String get() {
            return "getA";
        }
    }

    public static class NonRootResourceB {
        @GET
        public String get() {
            return "getB";
        }
    }

    @Test
    public void testResourceModelWithNonRootResources() {
        ResourceModel.Builder builder = new ResourceModel.Builder(false);
        builder.addResource(Resource.from(NonRootResourceA.class));
        builder.addResource(Resource.from(NonRootResourceB.class));
        final ResourceModel model = builder.build();
        assertEquals(2, model.getResources().size());
        assertEquals(0, model.getRootResources().size());
    }

    @Test
    public void testSubResourceModelWithNonRootResources() {
        ResourceModel.Builder builder = new ResourceModel.Builder(true);
        builder.addResource(Resource.from(NonRootResourceA.class));
        builder.addResource(Resource.from(NonRootResourceB.class));
        final ResourceModel model = builder.build();
        assertEquals(1, model.getResources().size());
        assertEquals(0, model.getRootResources().size());
    }
}
