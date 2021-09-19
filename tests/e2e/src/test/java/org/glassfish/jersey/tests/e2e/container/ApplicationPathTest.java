/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.container;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory;
import org.glassfish.jersey.test.netty.NettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class ApplicationPathTest extends JerseyContainerTest {


    private static final List<TestContainerFactory> FACTORIES = listContainerFactories(
            new GrizzlyTestContainerFactory(),
            new JdkHttpServerTestContainerFactory(),
            new NettyTestContainerFactory()
    );

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestContainerFactory[]> parameters() throws Exception {
        return FACTORIES.stream().map(input -> new TestContainerFactory[]{input}).collect(Collectors.toList());
    }

    @ApplicationPath("applicationpath")
    public static class ApplicationPathTestApplication extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(ApplicationPathResourceTest.class);
        }
    }

    @Path("/resource")
    public static class ApplicationPathResourceTest {
        @GET
        public String hello() {
            return "HelloWorld!";
        }
    }

    @Override
    protected Application configure() {
        return new ApplicationPathTestApplication();
    }

    @Test
    public void testApplicationPath() {
        try (Response response = target("applicationpath").path("resource").request().get()) {
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(new ApplicationPathResourceTest().hello(), response.readEntity(String.class));
        }

        try (Response response = target("").path("resource").request().get()) {
            Assert.assertEquals(404, response.getStatus());
        }
    }

}
