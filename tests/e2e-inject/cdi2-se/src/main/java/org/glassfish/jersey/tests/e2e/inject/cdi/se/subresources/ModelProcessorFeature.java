/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.subresources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;

public class ModelProcessorFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(SimpleModelProcessor.class);
        return true;
    }

    @Priority(5000)
    public static class SimpleModelProcessor implements ModelProcessor {

        @Override
        public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
            ResourceModel.Builder builder = new ResourceModel.Builder(resourceModel.getRootResources(), false);
            final Resource singletonResource = Resource.from(SingletonResource.class);
            builder.addResource(singletonResource);

            final Resource requestScopeResource = Resource.from(RequestScopeResource.class);
            builder.addResource(requestScopeResource);

            final Resource.Builder resourceBuilder = Resource.builder("instance");
            resourceBuilder.addMethod("GET").handledBy(new Inflector<ContainerRequestContext, String>() {
                private int counter = 0;

                @Override
                public String apply(ContainerRequestContext containerRequestContext) {
                    return String.valueOf("Inflector:" + counter++);
                }
            });
            final Resource instanceResource = resourceBuilder.build();

            builder.addResource(instanceResource);

            return builder.build();
        }

        @Override
        public ResourceModel processSubResource(ResourceModel subResource, Configuration configuration) {
            final Resource resource = Resource.builder()
                    .mergeWith(Resource.from(EnhancedSubResourceSingleton.class))
                    .mergeWith(Resource.from(EnhancedSubResource.class))
                    .mergeWith(subResource.getResources().get(0)).build();

            return new ResourceModel.Builder(true).addResource(resource).build();
        }
    }

    @Singleton
    public static class EnhancedSubResourceSingleton {
        private int counter = 0;

        @GET
        @Path("enhanced-singleton")
        public String get() {
            return "EnhancedSubResourceSingleton:" + String.valueOf(counter++);
        }
    }

    public static class EnhancedSubResource {

        private int counter = 0;

        @GET
        @Path("enhanced")
        public String get() {
            return String.valueOf("EnhancedSubResource:" + counter++);
        }
    }

    @Path("request-scope")
    public static class RequestScopeResource {
        private int counter = 0;

        @GET
        public String get() {
            return String.valueOf("RequestScopeResource:" + counter++);
        }
    }

    @Path("singleton")
    @Singleton
    public static class SingletonResource {
        private int counter = 0;

        @GET
        public String get() {
            return String.valueOf("SingletonResource:" + counter++);
        }
    }
}
