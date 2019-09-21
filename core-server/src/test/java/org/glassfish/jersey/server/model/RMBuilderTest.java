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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Runtime model builder test.
 *
 * @author Jakub Podlesak
 */
public class RMBuilderTest {

    @Path("/helloworld")
    public static class HelloWorldResource {

        @GET
        @Produces("text/plain")
        public String getHello() {
            return "hello";
        }

        @OPTIONS
        @Produces("text/plain")
        public String getOptions() {
            return "GET";
        }

        @GET
        @Path("another/{b}")
        @Produces("text/plain")
        public String getAnother() {
            return "another";
        }
    }

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Test
    public void testHelloWorld() throws Exception {
        ApplicationHandler app = createApplication(HelloWorldResource.class);
        ContainerResponse response = app.apply(RequestContextBuilder.from("/helloworld", "GET").build()).get();

        assertEquals("hello", response.getEntity());
    }

    @Test
    public void testOptions() throws Exception {
        ApplicationHandler app = createApplication(HelloWorldResource.class);
        ContainerResponse response = app.apply(RequestContextBuilder.from("/helloworld", "OPTIONS")
                .accept(MediaType.TEXT_PLAIN_TYPE).build()).get();

        assertEquals("GET", response.getEntity());
    }

    @Test
    public void testSubResMethod() throws Exception {
        ApplicationHandler app = createApplication(HelloWorldResource.class);
        ContainerResponse response = app.apply(RequestContextBuilder.from("/helloworld/another/b", "GET").build()).get();

        assertEquals("another", response.getEntity());
    }
}
