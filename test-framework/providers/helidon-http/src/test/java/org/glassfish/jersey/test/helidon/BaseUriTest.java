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

package org.glassfish.jersey.test.helidon;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseUriTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new HelidonTestContainerFactory();
    }

    @Path("root")
    public static class TestResource {
        @GET
        public String get() {
            return "GET";
        }

        @Path("sub")
        @GET
        public String getSub() {
            return "sub";
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return DeploymentContext.builder(new ResourceConfig(TestResource.class))
                .contextPath("context1/context2")
                .build();
    }

    @Test
    public void testGet() {
        final WebTarget target = target("root");

        final String s = target.request().get(String.class);
        Assertions.assertEquals("GET", s);
    }

    @Test
    public void testGetSub() {
        final WebTarget target = target("root/sub");

        final String s = target.request().get(String.class);
        Assertions.assertEquals("sub", s);
    }

}
