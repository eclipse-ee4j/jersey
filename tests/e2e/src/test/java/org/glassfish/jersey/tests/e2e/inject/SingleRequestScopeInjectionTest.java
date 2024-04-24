/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.GenericType;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleRequestScopeInjectionTest extends JerseyTest {
    @Path("hello")
    public static class HelloResource {
        @GET
        public String getHello() {
            return "Hello World!";
        }
    }
    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(HelloResource.class);
        resourceConfig.register(new InjectedFilterRegistrar(InjectedFilter.class));
        return resourceConfig;
    }
    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }
    @Test
    public void test() {
        final String hello = target("hello").request().get(String.class);
        assertEquals("Hello World!", hello);
    }
    public static class InjectedFilter implements ContainerRequestFilter {
        @Inject
        private InjectionManager injectionManager;
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            Ref<HttpServletRequest> requestRef =
                    injectionManager.getInstance((new GenericType<Ref<HttpServletRequest>>() {}).getType());
            if (requestRef == null || requestRef.get() == null) {
                throw new IllegalStateException("Request not injected");
            }
        }
    }
    public static class InjectedFilterRegistrar implements DynamicFeature {
        private final Class<?> filterToRegister;
        private final AtomicInteger ai = new AtomicInteger(0);
        public InjectedFilterRegistrar(Class<?> filterToRegister) {
            this.filterToRegister = filterToRegister;
        }
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (ai.get() == 1) {
                context.register(filterToRegister);
            }
        }
    }
}