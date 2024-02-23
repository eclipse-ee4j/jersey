/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.scanning;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Martin Snyder
 */
public class JarFileScannerTest {

    private String jaxRsApiPath;

    @BeforeEach
    public void setUp() throws Exception {
        final String classPath = System.getProperty("java.class.path");
        final String[] entries = classPath.split(System.getProperty("path.separator"));

        for (final String entry : entries) {
            if (entry.contains("jakarta.ws.rs-api")) {
                jaxRsApiPath = entry;
                break;
            }
        }

        if (jaxRsApiPath == null) {
            fail("Could not find jakarta.ws.rs-api.");
        }
    }

    @Test
    public void testRecursiveResourceEnumerationOfAllPackages() throws IOException {
        final int actualEntries = countJarEntriesByPattern(Pattern.compile(".*\\.(class|properties|xml|md)"));
        final int scannedEntries = countJarEntriesUsingScanner("", true);
        assertThat("Failed to enumerate all contents of jakarta.ws.rs-api", scannedEntries, equalTo(actualEntries));
    }

    @Test
    public void testRecursiveClassEnumerationWithExistantPackage() throws IOException {
        final int actualEntries = countJarEntriesByPattern(Pattern.compile("jakarta/ws/rs/.*\\.class"));
        final int scannedEntries = countJarEntriesUsingScanner("jakarta/ws/rs", true);
        assertThat("Failed to enumerate all contents of jakarta.ws.rs-api", scannedEntries, equalTo(actualEntries));
    }

    @Test
    public void testNonRecursiveClassEnumerationWithExistantPackage() throws IOException {
        final int actualEntries = countJarEntriesByPattern(Pattern.compile("jakarta/ws/rs/[^/]*\\.class"));
        final int scannedEntries = countJarEntriesUsingScanner("jakarta/ws/rs", false);
        assertThat("Failed to enumerate package 'jakarta.ws.rs' of jakarta.ws.rs-api", scannedEntries, equalTo(actualEntries));
    }

    @Test
    public void testRecursiveClassEnumerationWithOptionalTrailingSlash() throws IOException {
        final int scannedEntriesWithoutSlash = countJarEntriesUsingScanner("jakarta/ws/rs", true);
        final int scannedEntriesWithSlash = countJarEntriesUsingScanner("jakarta/ws/rs/", true);
        assertThat("Adding a trailing slash incorrectly affects recursive scanning", scannedEntriesWithSlash,
                equalTo(scannedEntriesWithoutSlash));
    }

    @Test
    public void testNonRecursiveClassEnumerationWithOptionalTrailingSlash() throws IOException {
        final int scannedEntriesWithoutSlash = countJarEntriesUsingScanner("jakarta/ws/rs", false);
        final int scannedEntriesWithSlash = countJarEntriesUsingScanner("jakarta/ws/rs/", false);
        assertThat("Adding a trailing slash incorrectly affects recursive scanning", scannedEntriesWithSlash,
                equalTo(scannedEntriesWithoutSlash));
    }

    private int countJarEntriesByPattern(final Pattern pattern) throws IOException {
        int matchingEntries = 0;

        try (final JarFile jarFile = new JarFile(this.jaxRsApiPath)) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (pattern.matcher(entry.getName()).matches()) {
                    matchingEntries++;
                }
            }
        }

        return matchingEntries;
    }

    private int countJarEntriesUsingScanner(final String parent, final boolean recursive) throws IOException {
        int scannedEntryCount = 0;

        try (final InputStream jaxRsApi = Files.newInputStream(Paths.get(this.jaxRsApiPath))) {
            final JarFileScanner jarFileScanner = new JarFileScanner(jaxRsApi, parent, recursive);
            while (jarFileScanner.hasNext()) {
                // Fetch next entry.
                jarFileScanner.next();

                // JERSEY-2175 and JERSEY-2197:
                // This test doesn't actually do anything with the input stream, but it is important that it
                // open/close the stream to simulate actual usage.  The reported defect is only exposed if you
                // call open/close in some fashion.
                try (final InputStream classStream = jarFileScanner.open()) {
                    scannedEntryCount++;
                }
            }
        }

        return scannedEntryCount;
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testClassEnumerationWithNonexistentPackage(final boolean recursive) throws IOException {
        try (final InputStream jaxRsApi = Files.newInputStream(Paths.get(this.jaxRsApiPath))) {
            final JarFileScanner jarFileScanner = new JarFileScanner(jaxRsApi, "jakarta/ws/r", recursive);
            assertFalse(jarFileScanner.hasNext(), "Unexpectedly found package 'jakarta.ws.r' in jakarta.ws.rs-api");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testClassEnumerationWithClassPrefix(final boolean recursive) throws IOException {
        try (final InputStream jaxRsApi = Files.newInputStream(Paths.get(this.jaxRsApiPath))) {
            final JarFileScanner jarFileScanner = new JarFileScanner(jaxRsApi, "jakarta/ws/rs/GE", recursive);
            assertFalse(jarFileScanner.hasNext(), "Unexpectedly found package 'jakarta.ws.rs.GE' in jakarta.ws.rs-api");
        }
    }
}
