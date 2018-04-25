/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test of resource path overriding via programmatic API.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ResourcePathOverrideTest {

    private ApplicationHandler createApplication(ResourceConfig resourceConfig) {
        return new ApplicationHandler(resourceConfig);
    }

    /**
     * Test resource.
     */
    @Path("hello")
    @Produces("text/plain")
    public static class HelloResource {

        @GET
        public String sayHello() {
            return "Hello!";
        }
    }

    @Test
    public void testOverride() throws Exception {
        ResourceConfig resourceConfig = new ResourceConfig(HelloResource.class);


        Resource.Builder resourceBuilder = Resource.builder(HelloResource.class)
                .path("hello2");
        resourceBuilder.addChildResource("world").addMethod("GET").produces("text/plain")
                .handledBy(new Inflector<ContainerRequestContext, String>() {

                    @Override
                    public String apply(ContainerRequestContext request) {
                        return "Hello World!";
                    }
                });

        final Resource resource = resourceBuilder.build();
        resourceConfig.registerResources(resource);

        ApplicationHandler app = createApplication(resourceConfig);

        ContainerRequest request;
        request = RequestContextBuilder.from("/hello", "GET").build();
        assertEquals("Hello!", app.apply(request).get().getEntity());

        request = RequestContextBuilder.from("/hello2", "GET").build();
        assertEquals("Hello!", app.apply(request).get().getEntity());

        request = RequestContextBuilder.from("/hello2/world", "GET").build();
        final ContainerResponse response = app.apply(request).get();
        assertEquals(200, response.getStatus());
        assertEquals("Hello World!", response.getEntity());
    }
}
