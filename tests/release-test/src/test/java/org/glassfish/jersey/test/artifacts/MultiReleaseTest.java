/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.artifacts;

import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class MultiReleaseTest {
    private static final String s = "";
    private static final File localRepository = MavenUtil.getLocalMavenRepository();
    private static final Properties properties = MavenUtil.getMavenProperties();

    private static final DependencyPair[] jdk11multiRelease = jdk11multiRelease(properties);
    private static final DependencyPair[] jdk12multiRelease = jdk12multiRelease(properties);
    private static final DependencyPair[] jdk17multiRelease = jdk17multiRelease(properties);
    private static final DependencyPair[] jdk21multiRelease = jdk21multiRelease(properties);

    @Test
    public void testIsJdkMultiRelease() throws IOException, XmlPullParserException {
        TestResult result = testJdkVersions("11", jdk11multiRelease);
        result.append(testJdkVersions("12", jdk12multiRelease));
        result.append(testJdkVersions("17", jdk17multiRelease));
        result.append(testJdkVersions("21", jdk21multiRelease));
        //Assertions.assertTrue(result.result(), "Some error occurred, see previous messages");
        Assert.assertTrue("Some error occurred, see previous messages", result.result());
    }

    private static TestResult testJdkVersions(String version, DependencyPair... dependencies)
            throws XmlPullParserException, IOException {
        final TestResult result = new TestResult();
        if (dependencies == null || dependencies.length == 0) {
            System.out.append("No dependencies found for jdk ").println(version);
            return result;
        }

        Stream<Dependency> deps = MavenUtil.streamJerseyJars();
        List<File> files = MavenUtil.keepJerseyJars(deps, dependencies)
                .map(dependency -> MavenUtil.getArtifactJar(localRepository, dependency, properties))
                .collect(Collectors.toList());

        //Assertions.assertEquals(dependencies.length, files.size(), "Some jdk " + version + " dependencies not found");
        if (dependencies.length != files.size()) {
            System.out.println("Expected:");
            for (DependencyPair pair : dependencies) {
                System.out.println(pair);
            }
            System.out.println("Resolved:");
            for (File file : files) {
                System.out.println(file.getName());
            }
            Assert.assertEquals("Some jdk " + version + " dependencies not found", dependencies.length, files.size());
        }

        for (File jar : files) {
            JarFile jarFile = new JarFile(jar);
            if (!jarFile.isMultiRelease()) {
                result.exception().append("Not a multirelease jar ").append(jar.getName()).println("!");
            }
            ZipEntry versions = jarFile.getEntry("META-INF/versions/" + version);
            System.out.append("Accessing META-INF/versions/").append(version).append(" of ").println(jar.getName());
            if (versions == null) {
                result.exception().append("No classes for JDK ").append(version).append(" for ").println(jar.getName());
            }
            result.ok().append("Classes for JDK ").append(version).append(" found for ").println(jar.getName());

            Optional<JarEntry> file = jarFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> !entry.getName().contains("versions"))
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .findAny();
            JarEntry jarEntry = file.get();
            result.append(ClassVersionChecker.checkClassVersion(jarFile, jarEntry, properties));
        }

        // Verify that number of multirelease jars matches the expected dependencies
        StringBuilder multi = new StringBuilder();
        int multiCnt = 0;
        List<File> allFiles = MavenUtil.streamJerseyJars()
                .map(dependency -> MavenUtil.getArtifactJar(localRepository, dependency, properties))
                .collect(Collectors.toList());
        for (File jar : files) {
            JarFile jarFile = new JarFile(jar);
            if (jarFile.isMultiRelease()) {
                multiCnt++;
                multi.append("Multirelease jar ").append(jar.getName()).append('\n');
            }
        }
        if (files.size() == multiCnt) {
            result.ok().println("There is expected number of multirelease jars");
        } else {
            result.exception().println("There is unexpected number of multirelease jars:");
            result.exception().append(multi).println("");
        }

        return result;
    }

    private static DependencyPair[] jdk11multiRelease(Properties properties) {
        String jerseyVersion = MavenUtil.getJerseyVersion(properties);
        if (jerseyVersion.startsWith("3.0")) {
            return jdk11multiRelease30();
        } else if (jerseyVersion.startsWith("3")) {
            return jdk11multiRelease31();
        }
        return jdk11multiRelease2();
    }

    private static DependencyPair[] jdk11multiRelease2() {
        return new DependencyPair[] {
                new DependencyPair("org.glassfish.jersey.bundles", "jaxrs-ri"),
                new DependencyPair("org.glassfish.jersey.core", "jersey-common")
        };
    }

    private static DependencyPair[] jdk11multiRelease30() {
        DependencyPair[] pairs30 = {
                new DependencyPair("org.glassfish.jersey.containers", "jersey-container-jetty-http"),
                new DependencyPair("org.glassfish.jersey.containers", "jersey-container-jetty-servlet"),
                new DependencyPair("org.glassfish.jersey.test-framework.providers", "jersey-test-framework-provider-jetty")
        };
        return DependencyPair.concat(jdk11multiRelease2(), pairs30);
    }

    private static DependencyPair[] jdk11multiRelease31() {
        return new DependencyPair[]{};
    }

    private static DependencyPair[] jdk12multiRelease(Properties properties) {
        return new DependencyPair[]{
                new DependencyPair("org.glassfish.jersey.ext", "jersey-wadl-doclet")
        };
    }

    private static DependencyPair[] jdk17multiRelease(Properties properties) {
        String jerseyVersion = MavenUtil.getJerseyVersion(properties);
        if (jerseyVersion.startsWith("3.0")) {
            return new DependencyPair[] {
                    new DependencyPair("org.glassfish.jersey.connectors", "jersey-helidon-connector"),
                    new DependencyPair("org.glassfish.jersey.connectors", "jersey-jetty-connector"),
                    new DependencyPair("org.glassfish.jersey.containers", "jersey-container-jetty-http"),
                    new DependencyPair("org.glassfish.jersey.ext", "jersey-spring6")
            };
        } else if (jerseyVersion.startsWith("3")) {
            return new DependencyPair[] {
                    new DependencyPair("org.glassfish.jersey.connectors", "jersey-helidon-connector"),
                    new DependencyPair("org.glassfish.jersey.connectors", "jersey-jetty-connector"),
                    new DependencyPair("org.glassfish.jersey.connectors", "jersey-jetty-http2-connector"),
                    new DependencyPair("org.glassfish.jersey.containers", "jersey-container-jetty-http"),
                    new DependencyPair("org.glassfish.jersey.containers", "jersey-container-jetty-http2"),
                    new DependencyPair("org.glassfish.jersey.test-framework.providers", "jersey-test-framework-provider-jetty"),
                    new DependencyPair("org.glassfish.jersey.test-framework.providers",
                            "jersey-test-framework-provider-jetty-http2"),
                    new DependencyPair("org.glassfish.jersey.ext", "jersey-spring6")
            };
        }
        return new DependencyPair[]{};
    }

    private static DependencyPair[] jdk21multiRelease(Properties properties) {
        String jerseyVersion = MavenUtil.getJerseyVersion(properties);
        if (jerseyVersion.startsWith("4")) {
            return new DependencyPair[]{
                    new DependencyPair("org.glassfish.jersey.core", "jersey-common")
            };
        } else {
            return new DependencyPair[]{
                    new DependencyPair("org.glassfish.jersey.bundles", "jaxrs-ri"),
                    new DependencyPair("org.glassfish.jersey.core", "jersey-common")
            };
        }
    }
}
