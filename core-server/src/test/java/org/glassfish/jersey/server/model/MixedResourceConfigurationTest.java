/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test of mixed (programmatic and annotation-based) resource configuration.
 *
 * @author Pavel Bucek
 */
public class MixedResourceConfigurationTest {

    static volatile String name = "Lady";

    @Path("/name")
    public static class NameResource {

        @GET
        @Produces("text/plain")
        public String getMethod() {
            return name;
        }
    }

    @Test
    public void testPutGet() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(NameResource.class);
        final Resource.Builder resourceBuilder = Resource.builder("/name");
        resourceBuilder.addMethod("PUT").handledBy(new Inflector<ContainerRequestContext, Response>() {

            @Override
            public Response apply(ContainerRequestContext request) {
                name = ((ContainerRequest) request).readEntity(String.class);
                return Response.ok().build();
            }
        });
        resourceConfig.registerResources(resourceBuilder.build());
        final ApplicationHandler application = new ApplicationHandler(resourceConfig);

        final ContainerResponse response = application.apply(
                RequestContextBuilder.from("/name", "PUT").entity("Gaga").type(MediaType.TEXT_PLAIN).build()).get();
        assertEquals(200, response.getStatus());

        assertEquals("Gaga", application.apply(
                RequestContextBuilder.from("/name", "GET").accept(MediaType.TEXT_PLAIN).build()).get().getEntity());
    }
}
