/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ResourceConfigBuilderTest {
    @Test
    public void testEmpty() {
        ResourceConfig resourceConfig = new ResourceConfig();

        assertTrue(resourceConfig.getClasses() != null);
        assertTrue(resourceConfig.getClasses().isEmpty());

        assertTrue(resourceConfig.getSingletons() != null);
        assertTrue(resourceConfig.getSingletons().isEmpty());

    }

    @Test
    public void testClasses() {
        ResourceConfig resourceConfig = new ResourceConfig(ResourceConfigBuilderTest.class);

        assertTrue(resourceConfig.getClasses() != null);
        assertTrue(resourceConfig.getClasses().size() == 1);
        assertTrue(resourceConfig.getClasses().contains(ResourceConfigBuilderTest.class));

        assertTrue(resourceConfig.getSingletons() != null);
        assertTrue(resourceConfig.getSingletons().isEmpty());
    }

    @Test
    public void testSingletons() {
        final ResourceConfigBuilderTest resourceConfigBuilderTest = new ResourceConfigBuilderTest();

        ResourceConfig resourceConfig = new ResourceConfig().registerInstances(resourceConfigBuilderTest);

        assertTrue(resourceConfig.getClasses() != null);
        assertTrue(resourceConfig.getClasses().isEmpty());

        assertTrue(resourceConfig.getSingletons() != null);
        assertTrue(resourceConfig.getSingletons().size() == 1);
        assertTrue(resourceConfig.getSingletons().contains(resourceConfigBuilderTest));
    }

    @Test
    public void testApplication() {
        final javax.ws.rs.core.Application application = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return super.getClasses();
            }

            @Override
            public Set<Object> getSingletons() {
                return super.getSingletons();
            }
        };

        ApplicationHandler ah = new ApplicationHandler(application);
        assertTrue(ah.getInjectionManager().getInstance(Application.class).equals(application));
    }

    /**
     * test that I can initialize resource config with application class instead of an application instance
     * and then read the app properties
     */
    @Test
    public void testApplicationClassProperties() {
        ResourceConfig resourceConfig = initApp(MyApplication.class);

        assertTrue(resourceConfig.getProperties().containsKey("myProperty"));
        assertTrue(resourceConfig.getProperties().get("myProperty").equals("myValue"));
    }

    /**
     * test that I can initialize resource config with application class instead of an application instance
     * and then read the app classes
     */
    @Test
    public void testApplicationClassClasses() {
        ResourceConfig resourceConfig = initApp(MyApplication2.class);

        assertTrue(!resourceConfig.getClasses().isEmpty());
    }

    private static class MyApplication extends ResourceConfig {
        public MyApplication() {
            property("myProperty", "myValue");
        }
    }

    public static class MyApplication2 extends ResourceConfig {
        public MyApplication2() {
            super(MyResource.class);
        }
    }

    @Path("resource")
    public static class MyResource {
        @GET
        public String getIt() {
            return "get it";
        }
    }

    private static ResourceConfig initApp(Class<? extends Application> appClass) {
        return new ApplicationHandler(appClass).getConfiguration();
    }

    public static class TestProvider implements ReaderInterceptor {

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            return context.proceed();
        }
    }

    // Reproducer JERSEY-1637
    @Test
    public void testRegisterNullOrEmptyContracts() {
        final TestProvider provider = new TestProvider();

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(TestProvider.class, (Class<?>[]) null);
        assertFalse(resourceConfig.getConfiguration().isRegistered(TestProvider.class));

        resourceConfig.register(provider,  (Class<?>[]) null);
        assertFalse(resourceConfig.getConfiguration().isRegistered(TestProvider.class));
        assertFalse(resourceConfig.getConfiguration().isRegistered(provider));

        resourceConfig.register(TestProvider.class,  new Class[0]);
        assertFalse(resourceConfig.getConfiguration().isRegistered(TestProvider.class));

        resourceConfig.register(provider,  new Class[0]);
        assertFalse(resourceConfig.getConfiguration().isRegistered(TestProvider.class));
        assertFalse(resourceConfig.getConfiguration().isRegistered(provider));
    }
}
