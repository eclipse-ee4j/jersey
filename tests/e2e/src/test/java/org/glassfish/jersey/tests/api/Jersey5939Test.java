/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Check the reused response does not leak by additional Annotations (ContainerResponse#setEntityAnnottaions.
 */
public class Jersey5939Test extends JerseyTest {

    private final List<ContainerResponse> capturedResponses = new ArrayList<>();

    @Override
    protected Application configure() {
        return new ResourceConfig(Restlet.class).register(new ContainerResponseFilter() {
            @Override
            public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                    throws IOException {
                if (responseContext instanceof ContainerResponse) {
                    capturedResponses.add((ContainerResponse) responseContext);
                }
            }
        });
    }

    @Test
    public void testIssue5939() {
        for (int i = 0; i < 10; i++) {
            Response response = target("foo/bar").request().get();
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
            ContainerResponse containerResponse = capturedResponses.get(i);
            Annotation[] annotations = containerResponse.getEntityAnnotations();
            // [@javax.ws.rs.GET(), @javax.ws.rs.Path("/bar")]
            assertEquals(2, annotations.length, "Found " + annotations.length + " annotations, in iteration " + i);
        }
    }

    @Path("/foo")
    public static class Restlet {

        private static final Response RESPONSE_204 = Response.noContent().build();

        @GET
        @Path("/bar")
        public Response fooBar() {
            return RESPONSE_204;
        }
    }
}