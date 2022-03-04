/*
 * Copyright (c) 2014, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal;

import static org.glassfish.jersey.server.JarUtils.createJarFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Configurable;

import org.glassfish.jersey.internal.ServiceConfigurationError;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.internal.ServiceFinder.ServiceLookupIteratorProvider;
import org.glassfish.jersey.internal.ServiceFinder.ServiceReflectionIteratorProvider;
import org.glassfish.jersey.server.JarUtils;
import org.glassfish.jersey.server.JaxRsFeatureRegistrationTest.DynamicFeatureImpl;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Michal Gajdos
 */
public class ServiceFinderTest {

    @AfterClass
    public static void afterClass() {
        // Restore the default
        ServiceFinder.setIteratorProvider(null);
    }

    @Test
    public void testExistingClass() {
        ServiceIteratorProvider[] providers = new ServiceIteratorProvider[] {
                new ServiceReflectionIteratorProvider(), new ServiceLookupIteratorProvider()};
        for (ServiceIteratorProvider provider : providers) {
            ServiceFinder.setIteratorProvider(provider);
            ServiceFinder<?> serviceFinder = ServiceFinder.find(DynamicFeature.class);
            checks(provider, serviceFinder);
            serviceFinder = ServiceFinder.find("jakarta.ws.rs.container.DynamicFeature");
            checks(provider, serviceFinder);
        }
    }

    @Test
    public void testMissingService() {
        ServiceIteratorProvider[] providers = new ServiceIteratorProvider[] {
                new ServiceReflectionIteratorProvider(), new ServiceLookupIteratorProvider()};
        for (ServiceIteratorProvider provider : providers) {
            ServiceFinder.setIteratorProvider(provider);
            ServiceFinder<?> serviceFinder = ServiceFinder.find(Configurable.class);
            assertFalse(serviceFinder.iterator().hasNext());
            serviceFinder = ServiceFinder.find("jakarta.ws.rs.core.Configurable");
            assertFalse(serviceFinder.iterator().hasNext());
        }
    }

    @Test
    public void testClassNotFound() {
        ServiceIteratorProvider[] providers = new ServiceIteratorProvider[] {
                new ServiceReflectionIteratorProvider(), new ServiceLookupIteratorProvider()};
        for (ServiceIteratorProvider provider : providers) {
            ServiceFinder.setIteratorProvider(provider);
            ServiceFinder<?> serviceFinder = ServiceFinder.find("doesNotExist");
            assertFalse(serviceFinder.iterator().hasNext());
        }
    }

    @Test
    public void testServiceReflectionIteratorProviderImplementationNotFound() {
        ServiceFinder.setIteratorProvider(new ServiceReflectionIteratorProvider());
        ServiceFinder<?> serviceFinder = ServiceFinder.find(ServiceExample.class, true);
        assertFalse(serviceFinder.iterator().hasNext());
        serviceFinder = ServiceFinder.find(ServiceExample.class, false);
        try {
            serviceFinder.iterator().hasNext();
            fail("It is expected to fail");
        } catch (ServiceConfigurationError e) {
            // Expected
        }
    }

    @Test
    public void testServiceLookupIteratorProviderImplementationNotFound() {
        ServiceFinder.setIteratorProvider(new ServiceLookupIteratorProvider());
        ServiceFinder<?> serviceFinder = ServiceFinder.find(ServiceExample.class, true);
        Iterator<?> iterator = serviceFinder.iterator();
        try {
            iterator.hasNext();
            iterator.next();
            fail("It is expected to fail");
        } catch (java.util.ServiceConfigurationError e) {
            // Expected
        }
        serviceFinder = ServiceFinder.find(ServiceExample.class, false);
        iterator = serviceFinder.iterator();
        try {
            iterator.hasNext();
            iterator.next();
            fail("It is expected to fail");
        } catch (java.util.ServiceConfigurationError e) {
            // Expected
        }
    }

    private void checks(ServiceIteratorProvider provider, ServiceFinder<?> serviceFinder) {
        Iterator<?> iterator = serviceFinder.iterator();
        assertTrue("No instance found with " + provider, iterator.hasNext());
        Object dynamicFeature = iterator.next();
        assertEquals(DynamicFeatureImpl.class, dynamicFeature.getClass());
    }

    @Test
    public void testJarTopLevel() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("org/glassfish/jersey/server/config/jaxrs-components", "META-INF/services/jaxrs-components");
        map.put("org/glassfish/jersey/server/config/toplevel/PublicRootResourceClass.class",
                "org/glassfish/jersey/server/config/toplevel/PublicRootResourceClass.class");

        final String path = ServiceFinderTest.class.getResource("").getPath();
        final ClassLoader classLoader = createClassLoader(path.substring(0, path.indexOf("org")), map);

        ServiceFinder.setIteratorProvider(new ServiceReflectionIteratorProvider());
        final ServiceFinder<?> finder = createServiceFinder(classLoader, "jaxrs-components");

        final Set<Class<?>> s = new HashSet<>();
        Collections.addAll(s, finder.toClassArray());

        assertTrue(s.contains(classLoader.loadClass("org.glassfish.jersey.server.config.toplevel.PublicRootResourceClass")));
        assertEquals(1, s.size());
    }

    private ServiceFinder<?> createServiceFinder(final ClassLoader cl, final String serviceName) throws IOException {
        final ClassLoader ocl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            final Class<?> prc = cl.loadClass("org.glassfish.jersey.internal.ServiceFinder");
            final Method m = prc.getMethod("find", String.class);

            return (ServiceFinder<?>) m.invoke(null, serviceName);
            // return new PackagesResourceConfig(packages);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ocl);
        }
    }

    private ClassLoader createClassLoader(final String base, final Map<String, String> entries) throws IOException {
        return createClassLoader(JarUtils.Suffix.jar, base, entries);
    }

    private ClassLoader createClassLoader(final JarUtils.Suffix s, final String base, final Map<String,
            String> entries) throws IOException {

        final URL[] us = new URL[1];
        us[0] = createJarFile(s, base, entries).toURI().toURL();
        return new PackageClassLoader(us);
    }

    private static class PackageClassLoader extends URLClassLoader {

        PackageClassLoader(final URL[] urls) {
            super(urls, null);
        }

        public Class<?> findClass(final String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (final ClassNotFoundException e) {
                return getSystemClassLoader().loadClass(name);
            }
        }
    }
}
