/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import org.glassfish.jersey.internal.OsgiRegistry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Utility class {@ling OsgiRegistry} tests.
 *
 * @author Stepan Vavra
 */
public class OsgiRegistryTest {

    @Test
    public void testWebInfClassesBundleEntryPathTranslation() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("org/glassfish/jersey", "/WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testWebInfClassesBundleEntryPathTranslationPackageTrailingSlash() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("org/glassfish/jersey/", "/WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testWebInfClassesBundleEntryPathTranslationPackageLeadingSlash() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("/org/glassfish/jersey", "/WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testWebInfClassesBundleEntryPathTranslationBundleNoLeadingSlash() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("/org/glassfish/jersey", "WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testOsgiInfClassesBundleEntryPathTranslation() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("/org/glassfish/jersey", "OSGI-INF/directory/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testBundleEntryPathTranslation() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/org/glassfish/jersey", "/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testBundleEntryPathTranslationBundleNoLeadingSlash() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/org/glassfish/jersey", "org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testBundleEntryPathTranslationNotMatching() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/com/oracle", "org/glassfish/jersey/Test.class");
        Assertions.assertEquals("com.oracle.Test", className);
    }

    @Test
    public void testWebInfClassesBundleEntryPathTranslationNotMatching() {
        String className = OsgiRegistry
                .bundleEntryPathToClassName("/com/oracle/", "/WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("com.oracle.Test", className);
    }

    @Test
    public void testWebInfClassesBundleEntryPathTranslationNotMatching2() {
        String className = OsgiRegistry.bundleEntryPathToClassName("com/oracle", "/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("com.oracle.Test", className);
    }

    @Test
    public void testRootBundleEntryPathTranslation() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/", "/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testRootBundleEntryPathTranslationNoLeadingSlash() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/", "org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testRootWebInfClassesBundleEntryPathTranslationNoLeadingSlash() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/", "/WEB-INF/classes/org/glassfish/jersey/Test.class");
        Assertions.assertEquals("org.glassfish.jersey.Test", className);
    }

    @Test
    public void testDotClassInPackageName() {
        String className = OsgiRegistry.bundleEntryPathToClassName("/", "com/classification/Test");
        Assertions.assertEquals("com.classification.Test", className);
    }

    @Test
    public void testRootWebInfClassesBundleEntryPathEsTranslation() {
        String className = OsgiRegistry.bundleEntryPathToClassName("es", "/WEB-INF/classes/es/a/Test.class");
        Assertions.assertEquals("es.a.Test", className);
    }

    @Test
    public void testIsTopLevelEntry() {
        Assertions.assertTrue(OsgiRegistry.isPackageLevelEntry("a", "/a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntrySequenceRepeats() {
        Assertions.assertFalse(OsgiRegistry.isPackageLevelEntry("o", "/a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntryNoSlash() {
        Assertions.assertTrue(OsgiRegistry.isPackageLevelEntry("a", "a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntrySequenceRepeatsNoSlash() {
        Assertions.assertFalse(OsgiRegistry.isPackageLevelEntry("o", "a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntrySlash() {
        Assertions.assertTrue(OsgiRegistry.isPackageLevelEntry("a/", "/a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntrySequenceRepeatsSlash() {
        Assertions.assertFalse(OsgiRegistry.isPackageLevelEntry("o/", "/a/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntryRoot() {
        Assertions.assertTrue(OsgiRegistry.isPackageLevelEntry("/", "/Foo.class"));
    }

    @Test
    public void testIsTopLevelEntryRootFalse() {
        Assertions.assertFalse(OsgiRegistry.isPackageLevelEntry("/", "/a/Foo.class"));
    }

}
