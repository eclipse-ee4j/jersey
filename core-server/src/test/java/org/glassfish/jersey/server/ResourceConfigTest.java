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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.server.config.innerstatic.InnerStaticClass;
import org.glassfish.jersey.server.config.toplevel.PublicRootResourceClass;
import org.glassfish.jersey.server.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;

import org.junit.Test;
import static org.glassfish.jersey.server.JarUtils.createJarFile;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import mockit.Mocked;
import mockit.Verifications;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ResourceConfigTest {

    @Test
    public void testGetElementsDefault1() {
        final String[] elements = Tokenizer.tokenize(new String[] {"a b,c;d\ne"});

        assertEquals(elements[0], "a");
        assertEquals(elements[1], "b");
        assertEquals(elements[2], "c");
        assertEquals(elements[3], "d");
        assertEquals(elements[4], "e");
    }

    @Test
    public void testGetElementsDefault2() {
        final String[] elements = Tokenizer.tokenize(new String[] {"a    b, ,c;d\n\n\ne"});

        assertEquals(elements[0], "a");
        assertEquals(elements[1], "b");
        assertEquals(elements[2], "c");
        assertEquals(elements[3], "d");
        assertEquals(elements[4], "e");
    }

    @Test
    public void testGetElementsExplicitDelimiter() {
        final String[] elements = Tokenizer.tokenize(new String[] {"a b,c;d\ne"}, " ;");

        assertEquals(elements[0], "a");
        assertEquals(elements[1], "b,c");
        assertEquals(elements[2], "d\ne");
    }

    @Test
    public void testResourceConfigClasses() {
        final ResourceConfig resourceConfig = new MyResourceConfig2();
        final ApplicationHandler ah = new ApplicationHandler(resourceConfig);

        assertTrue(ah.getConfiguration().getClasses().contains(MyResource.class));
    }

    @Test
    public void testResourceConfigInjection() throws InterruptedException, ExecutionException {
        final int rcId = 12345;
        final ResourceConfig resourceConfig = new MyResourceConfig2(rcId);
        final ApplicationHandler handler = new ApplicationHandler(resourceConfig);

        assertSame(resourceConfig, handler.getInjectionManager().getInstance(Application.class));

        final ContainerResponse r = handler.apply(RequestContextBuilder.from("/", "/resource?id=" + rcId, "GET").build()).get();
        assertEquals(200, r.getStatus());
        assertEquals("Injected application instance not same as used for building the Jersey handler.",
                "true", r.getEntity());
    }

    @Test
    public void testResourceConfigMergeApplications() throws Exception {
        // Add myBinder + one default.
        final MyOtherBinder defaultBinder = new MyOtherBinder();
        final ResourceConfig rc = ResourceConfig.forApplicationClass(MyResourceConfig1.class);
        rc.register(defaultBinder);
        final ApplicationHandler handler = new ApplicationHandler(rc);
        assertTrue(handler.getConfiguration().getComponentBag().getInstances(ComponentBag.BINDERS_ONLY).contains(defaultBinder));
    }

    @Test
    public void testApplicationName() {
        final ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        resourceConfig.setApplicationName("app");
        assertEquals("app", resourceConfig.getApplicationName());
        resourceConfig.lock();
        assertEquals("app", resourceConfig.getApplicationName());
    }

    @Test
    public void testApplicationNameDefinedByProperty() {
        final ResourceConfig resourceConfig = new ResourceConfig(MyResource.class);
        resourceConfig.property(ServerProperties.APPLICATION_NAME, "app");
        assertNull(resourceConfig.getApplicationName());
        resourceConfig.lock();
        assertEquals("app", resourceConfig.getApplicationName());
    }

    public static class MyResourceConfig1 extends ResourceConfig {

        public MyResourceConfig1() {
            property(ServerProperties.WADL_FEATURE_DISABLE, true);
            register(new MyBinder());
        }
    }

    public static class MyResourceConfig2 extends ResourceConfig {

        private final int id;

        public MyResourceConfig2() {
            this(0);
        }

        public MyResourceConfig2(final int id) {
            property(ServerProperties.WADL_FEATURE_DISABLE, true);
            this.id = id;
            registerClasses(MyResource.class);
        }
    }

    @Path("resource")
    public static class MyResource {

        @Context
        Application app;

        @GET
        public String test(@QueryParam("id") final int rcId) {
            return Boolean.toString((app instanceof MyResourceConfig2) && ((MyResourceConfig2) app).id == rcId);
        }
    }

    public static class MyBinder extends AbstractBinder {

        @Override
        protected void configure() {
            // do nothing
        }
    }

    public static class MyOtherBinder extends AbstractBinder {

        @Override
        protected void configure() {
            // do nothing
        }
    }

    @Test
    public void testClassPathPropertyTopLevel() {
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH, PublicRootResourceClass.class.getResource("").getPath());

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes.size(), is(1));
    }

    @Test
    public void testClassPathPropertyInnerStatic() {
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH, InnerStaticClass.class.getResource("").getPath());

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(1));
    }

    @Test
    public void testClassPathPropertyTopLevelInnerStatic() {
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH,
                        PublicRootResourceInnerStaticClass.class.getResource("").getPath());

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(2));
    }

    @Test
    public void testClassPathPropertyAll() {
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH, ResourceConfigTest.class.getResource("").getPath() + "/config");

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(4));
    }

    @Test
    public void testClassPathPropertyAllMultiplePaths() {
        final String paths = PublicRootResourceClass.class.getResource("").getPath() + ";"
                + InnerStaticClass.class.getResource("").getPath() + ";"
                + PublicRootResourceInnerStaticClass.class.getResource("").getPath();
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH, paths);

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(4));
    }

    @Test
    public void testClassPathPropertyAllMultiplePathsWithSpaces() {
        final String paths = PublicRootResourceClass.class.getResource("").getPath() + "; "
                + InnerStaticClass.class.getResource("").getPath() + ";;"
                + PublicRootResourceInnerStaticClass.class.getResource("").getPath() + "; ;; ";
        final ResourceConfig rc = new ResourceConfig()
                .property(ServerProperties.PROVIDER_CLASSPATH, paths);

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.class));
        assertThat(classes, hasItem(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(4));
    }

    @Test
    public void testClassPathPropertyJarTopLevel() throws Exception {
        final ResourceConfig rc = createConfigWithClassPathProperty(
                createJarFile(ResourceConfigTest.class.getResource("").getPath(),
                        "config/toplevel/PublicRootResourceClass.class",
                        "config/toplevel/PackageRootResourceClass.class")
        );

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes.size(), is(1));
    }

    @Test
    public void testClassPathPropertyJarInnerStatic() throws Exception {
        final ResourceConfig rc = createConfigWithClassPathProperty(
                createJarFile(ResourceConfigTest.class.getResource("").getPath(),
                        "config/innerstatic/InnerStaticClass.class",
                        "config/innerstatic/InnerStaticClass$PublicClass.class",
                        "config/innerstatic/InnerStaticClass$PackageClass.class",
                        "config/innerstatic/InnerStaticClass$ProtectedClass.class",
                        "config/innerstatic/InnerStaticClass$PrivateClass.class")
        );

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(1));
    }

    @Test
    public void testClassPathPropertyJarAll() throws Exception {
        final ResourceConfig rc = createConfigWithClassPathProperty(
                createJarFile(ResourceConfigTest.class.getResource("").getPath(),
                        "config/toplevel/PublicRootResourceClass.class",
                        "config/toplevel/PackageRootResourceClass.class",
                        "config/innerstatic/InnerStaticClass.class",
                        "config/innerstatic/InnerStaticClass$PublicClass.class",
                        "config/innerstatic/InnerStaticClass$PackageClass.class",
                        "config/innerstatic/InnerStaticClass$ProtectedClass.class",
                        "config/innerstatic/InnerStaticClass$PrivateClass.class")
        );

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(2));
    }

    @Test
    public void testClassPathPropertyZipAll() throws Exception {
        final ResourceConfig rc = createConfigWithClassPathProperty(
                createJarFile(JarUtils.Suffix.zip, ResourceConfigTest.class.getResource("").getPath(),
                        "config/toplevel/PublicRootResourceClass.class", "config/toplevel/PackageRootResourceClass.class",
                        "config/innerstatic/InnerStaticClass.class", "config/innerstatic/InnerStaticClass$PublicClass.class",
                        "config/innerstatic/InnerStaticClass$PackageClass.class",
                        "config/innerstatic/InnerStaticClass$ProtectedClass.class",
                        "config/innerstatic/InnerStaticClass$PrivateClass.class")
        );

        final Set<Class<?>> classes = rc.getClasses();
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
        assertThat(classes.size(), is(2));
    }

    /**
     * Reproducer for JERSEY-2796: Calling getClasses() on ResourceConfig breaks example in some cases.
     * <p/>
     * Registering a component after calling {@link ResourceConfig#getClasses()} and checking that all expected components are
     * registered.
     */
    @Test
    public void testGetClasses() throws Exception {
        final ResourceConfig rc = new ResourceConfig()
                .packages(false, PublicRootResourceClass.class.getPackage().getName());

        Set<Class<?>> classes = rc.getClasses();
        assertThat(classes.size(), is(1));
        assertThat(classes, hasItem(PublicRootResourceClass.class));

        rc.register(InnerStaticClass.PublicClass.class);

        classes = rc.getClasses();
        assertThat(classes.size(), is(2));
        assertThat(classes, hasItem(PublicRootResourceClass.class));
        assertThat(classes, hasItem(InnerStaticClass.PublicClass.class));
    }

    /**
     * Reproducer for OWLS-19790: Invalidate resource finders in resource config only when needed.
     */
    @Test
    public void testInvalidateResourceFinders(@Mocked final PackageNamesScanner scanner) throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages(false, PublicRootResourceClass.class.getPackage().getName());

        // Scan packages.
        resourceConfig.getClasses();

        // No reset.
        new Verifications() {{
            scanner.reset();
            times = 0;
        }};

        resourceConfig.register(InnerStaticClass.PublicClass.class);

        // Reset - we called getClasses() on ResourceConfig.
        new Verifications() {{
            scanner.reset();
            times = 1;
        }};

        // No reset.
        resourceConfig.register(PublicRootResourceClass.class);
        resourceConfig.register(PublicRootResourceInnerStaticClass.PublicClass.class);

        // No reset - simple registering does not invoke cache invalidation and reset of resource finders.
        new Verifications() {{
            scanner.reset();
            times = 1;
        }};

        // Scan packages.
        resourceConfig.getClasses();

        resourceConfig.registerFinder(new PackageNamesScanner(new String[] {"javax.ws.rs"}, false));

        // Reset - we called getClasses() on ResourceConfig.
        new Verifications() {{
            scanner.reset();
            times = 2;
        }};
    }

    @Test
    public void testResourceFinderStreamsClosed() throws IOException {
        System.out.println(new ResourceConfig().packages("javax.ws.rs").getClasses());
    }

    private ResourceConfig createConfigWithClassPathProperty(final File jarFile) {
        return new ResourceConfig().property(ServerProperties.PROVIDER_CLASSPATH, jarFile.getAbsolutePath());
    }
}
