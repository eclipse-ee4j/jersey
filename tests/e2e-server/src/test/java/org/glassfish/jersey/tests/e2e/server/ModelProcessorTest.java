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

package org.glassfish.jersey.tests.e2e.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

/**
 * Test model processor.
 *
 * @author Miroslav Fuksa
 *
 */
@RunWith(ConcurrentRunner.class)
public class ModelProcessorTest extends JerseyTest {

    public static class ModelProcessorFeature implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            context.register(SimpleModelProcessor.class);
            return true;
        }

        private static class SimpleModelProcessor implements ModelProcessor {

            @Override
            public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
                ResourceModel.Builder modelBuilder = new ResourceModel.Builder(false);
                final Resource modelResource = Resource.from(ModelResource.class);
                modelBuilder.addResource(modelResource);

                for (final Resource resource : resourceModel.getRootResources()) {
                    Resource newResource = enhanceResource(resource);
                    modelBuilder.addResource(newResource);
                }

                return modelBuilder.build();
            }

            private Resource enhanceResource(final Resource resource) {
                final Resource.Builder resBuilder = Resource.builder(resource);
                boolean optionsFound = false;
                for (ResourceMethod resourceMethod : resource.getResourceMethods()) {
                    if (resourceMethod.getHttpMethod().equals("OPTIONS")) {
                        optionsFound = true;
                    }
                }
                if (!optionsFound) {

                    resBuilder.addMethod("OPTIONS").produces(MediaType.TEXT_PLAIN_TYPE)
                            .handledBy(new Inflector<ContainerRequestContext, String>() {
                                @Override
                                public String apply(ContainerRequestContext containerRequestContext) {
                                    return resource.getPath();
                                }
                            });
                }

                final Inflector<ContainerRequestContext, Object> inflector = new Inflector<ContainerRequestContext, Object>() {

                    @Override
                    public Object apply(ContainerRequestContext requestContext) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("my-resource:");
                        final String path = resource.getPath();
                        sb.append(path == null ? "<no-path>" : path + ",");

                        for (ResourceMethod resourceMethod : sortResourceMethods(resource.getResourceMethods())) {
                            sb.append(resourceMethod.getHttpMethod()).append("=").append("")
                                    .append(resourceMethod.getInvocable().getHandlingMethod().getName()).append("|");
                        }
                        return sb.toString();
                    }
                };

                resBuilder.addChildResource("my-resource")
                        .addMethod("GET")
                        .produces(MediaType.TEXT_PLAIN_TYPE)
                        .handledBy(inflector).build();

                return resBuilder.build();
            }

            @Override
            public ResourceModel processSubResource(ResourceModel subResource, Configuration configuration) {
                final Resource resource = enhanceResource(subResource.getResources().get(0));
                return new ResourceModel.Builder(true).addResource(resource).build();
            }
        }

        @Path("model")
        public static class ModelResource {

            @Context
            ExtendedResourceContext resourceContext;

            @GET
            public String get() {
                final ResourceModel resourceModel = resourceContext.getResourceModel();
                StringBuilder sb = new StringBuilder();
                List<Resource> sortedResources = resourceModel.getRootResources();
                Collections.sort(sortedResources, new Comparator<Resource>() {
                    @Override
                    public int compare(Resource o1, Resource o2) {
                        final String path1 = o1.getPath() == null ? "" : o1.getPath();
                        final String path2 = o2.getPath() == null ? "" : o2.getPath();
                        return path1.compareTo(path2);
                    }
                });
                for (Resource resource : sortedResources) {
                    final String path = resource.getPath();
                    sb.append(path == null ? "<no-path>" : path).append("|");
                }

                return sb.toString();
            }
        }
    }

    private static List<ResourceMethod> sortResourceMethods(List<ResourceMethod> resourceMethods) {
        List<ResourceMethod> newList = new ArrayList<>(resourceMethods);
        Collections.sort(newList, (o1, o2) -> o1.getHttpMethod().compareTo(o2.getHttpMethod()));
        return newList;
    }

    @Path("a")
    public static class ResourceA {

        @GET
        public String getFromA() {
            return "a-get";
        }

        @POST
        public String postFromA(String entity) {
            return "a-post";
        }

        @GET
        @Path("child")
        public String getChild() {
            return "a-child-get";
        }

        @Path("locator")
        public SubResource locatorFromA() {
            return new SubResource();
        }
    }

    public static class SubResource {

        @GET
        public String getFromSubResource() {
            return "sub-get";
        }
    }

    @Path("b")
    public static class ResourceB {

        @GET
        public String getFromB() {
            return "b-get";
        }

        @OPTIONS
        public String optionsFromB() {
            return "b-options";
        }

        @Path("locator")
        public SubResource locatorFromB() {
            return new SubResource();
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = new ResourceConfig(ResourceA.class, ResourceB.class, ModelProcessorFeature.class);
        resourceConfig.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        return resourceConfig;
    }

    @Test
    public void testResourceAGet() {
        Response response = target("/a").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("a-get", entity);
    }

    @Test
    public void testResourceAPost() {
        Response response = target("/a").request().post(Entity.entity("post", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("a-post", entity);
    }

    @Test
    public void testResourceAOptions() {
        Response response = target("/a").request(MediaType.TEXT_PLAIN_TYPE).options();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("a", entity);
    }

    @Test
    public void testResourceAChildGet() {
        Response response = target("/a/child").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("a-child-get", entity);
    }

    @Test
    public void testResourceALocatorGet() {
        Response response = target("/a/locator").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("sub-get", entity);
    }

    @Test
    public void testResourceALocatorOptions() {
        Response response = target("/a/locator").request(MediaType.TEXT_PLAIN_TYPE).options();
        assertEquals(204, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("", entity);
    }

    @Test
    public void testResourceBGet() {
        Response response = target("/b").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("b-get", entity);
    }

    @Test
    public void testResourceBOptions() {
        Response response = target("/b").request(MediaType.TEXT_PLAIN_TYPE).options();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("b-options", entity);
    }

    @Test
    public void testResourceBLocatorGet() {
        Response response = target("/b/locator").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("sub-get", entity);
    }

    @Test
    public void testResourceBLocatorOptions() {
        Response response = target("/b/locator").request(MediaType.TEXT_PLAIN_TYPE).options();
        assertEquals(204, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("", entity);
    }

    @Test
    public void testResourceAMyResource() {
        Response response = target("/a/my-resource").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("my-resource:a,GET=getFromA|POST=postFromA|", entity);
    }

    @Test
    public void testResourceALocatorMyResource() {
        Response response = target("/a/locator/my-resource").request(MediaType.TEXT_PLAIN_TYPE).get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("my-resource:<no-path>GET=getFromSubResource|", entity);
    }

    @Test
    public void testResourceBMyResource() {
        Response response = target("/b/my-resource").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("my-resource:b,GET=getFromB|OPTIONS=optionsFromB|", entity);
    }

    @Test
    public void testInfo() {
        Response response = target("/model").request().get();
        assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("a|b|model|", entity);
    }

}
