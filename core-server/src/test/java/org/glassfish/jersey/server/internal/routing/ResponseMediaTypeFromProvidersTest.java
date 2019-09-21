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

package org.glassfish.jersey.server.internal.routing;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class ResponseMediaTypeFromProvidersTest {

    @Path("resource/subresource/sub")
    public static class AnotherSubResource {

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        public String sub() {
            return getClass().getSimpleName();
        }

        @POST
        public String subsub() {
            return sub() + sub();
        }

        @GET
        public String get() {
            return sub();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getget() {
            return subsub();
        }

        @GET
        @Produces("text/*")
        public String getTextStar() {
            return "text/*";
        }

        @POST
        @Consumes("text/*")
        public String postTextStar() {
            return "text/*";
        }
    }

    @Test
    public void testSubResource() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(AnotherSubResource.class);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ContainerResponse response = applicationHandler.apply(
                RequestContextBuilder.from("/resource/subresource/sub", "POST").header("Accept", "text/plain").build()).get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaderString("Content-Type"), equalTo("text/plain"));
        assertThat((String) response.getEntity(), equalTo("AnotherSubResource"));
    }
}
