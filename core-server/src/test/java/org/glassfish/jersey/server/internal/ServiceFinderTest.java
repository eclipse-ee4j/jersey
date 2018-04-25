/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.server.JarUtils;

import org.junit.Test;
import static org.glassfish.jersey.server.JarUtils.createJarFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class ServiceFinderTest {

    @Test
    public void testJarTopLevel() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("org/glassfish/jersey/server/config/jaxrs-components", "META-INF/services/jaxrs-components");
        map.put("org/glassfish/jersey/server/config/toplevel/PublicRootResourceClass.class",
                "org/glassfish/jersey/server/config/toplevel/PublicRootResourceClass.class");

        final String path = ServiceFinderTest.class.getResource("").getPath();
        final ClassLoader classLoader = createClassLoader(path.substring(0, path.indexOf("org")), map);

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
