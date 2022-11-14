/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationCachingTest extends JerseyTest {

    private static AtomicInteger singletonCounter = new AtomicInteger(0);

    public static class ApplicationCachingTestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {

        }
    }

    @Path("/")
    public static class ApplicationCachingTestResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public static class OneTimeCalledApplication extends Application {
        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> map = new HashMap<>();
            map.put(ServerProperties.WADL_FEATURE_DISABLE, true);
            return map;
        }

        @Override
        public Set<Object> getSingletons() {
            singletonCounter.incrementAndGet();
            return Collections.singleton(new ApplicationCachingTestFilter());
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(ApplicationCachingTestResource.class);
        }
    }

    @Override
    protected Application configure() {
        return new OneTimeCalledApplication();
    }

    @Test
    public void testOneTimeCalled() {
        try (Response r = target().request().get()) {
            Assertions.assertEquals(200, r.getStatus());
        }
        Assertions.assertEquals(1, singletonCounter.get());
    }
}
