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

package org.glassfish.jersey.server.internal.scanning;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarInputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link PackageNamesScanner}.
 *
 * @author Eric Navarro
 * @author Michal Gajdos
 */
@RunWith(JMockit.class)
public class PackageNamesScannerTest {

    private static final String[] packages = {"javax.ws.rs"};

    private String jaxRsApiPath;

    @Before
    public void setUp() throws Exception {
        final String classPath = System.getProperty("java.class.path");
        final String[] entries = classPath.split(System.getProperty("path.separator"));

        for (final String entry : entries) {
            if (entry.contains("javax.ws.rs-api")) {
                jaxRsApiPath = entry;
                break;
            }
        }

        if (jaxRsApiPath == null) {
            fail("Could not find javax.ws.rs-api.");
        }
    }

    @Test
    public void testWsJarScheme() {
        assertTrue("Expected at least one class to be found.",
                new PackageNamesScanner(createTestClassLoader("wsjar", createTestURLStreamHandler("wsjar"), jaxRsApiPath),
                        packages, false).hasNext());
    }

    @Test
    public void testJarScheme() {
        // Uses default class loader
        assertTrue("Expected at least one class to be found.", new PackageNamesScanner(packages, false).hasNext());
    }

    @Test
    public void testZipScheme() {
        assertTrue("Expected at least one class to be found.",
                new PackageNamesScanner(createTestClassLoader("zip", createTestURLStreamHandler("zip"), jaxRsApiPath),
                        packages, false).hasNext());
    }

    @Test(expected = ResourceFinderException.class)
    public void testInvalidScheme() {
        new PackageNamesScanner(createTestClassLoader("bad", createTestURLStreamHandler("bad"), jaxRsApiPath), packages, false);
    }


    @Tested
    PackageNamesScanner scanner1 = new PackageNamesScanner(new String[]{"javax.ws.rs"}, false);
    @Tested
    PackageNamesScanner scanner2 = new PackageNamesScanner(new String[]{"javax.ws.rs.core"}, false);
    @Tested
    PackageNamesScanner scanner3 = new PackageNamesScanner(new String[]{"javax.ws.rs.client"}, false);

    /**
     * Reproducer for OWLS-19790: When scanner is reset the underlying JAR input streams should be closed.
     */
    @Test
    public void testInputStreamClosedAfterReset() throws Exception {
        JarInputStream stream = new JarInputStream(
                new ByteArrayInputStream("test".getBytes(), 0, 4));

        new Expectations(InputStream.class){};

        scanner1.reset();

        scanner2.reset();
        scanner2.reset();

        scanner3.reset();

        new Verifications() {{
            stream.close();
            times = 4;
        }};
    }

    /**
     * Reproducer for OWLS-19790: When scanner is closed the underlying JAR input streams should be closed as well.
     */
    @Test
    public void testInputStreamClosedAfterClose()  throws Exception {

        JarInputStream stream = new JarInputStream(
                new ByteArrayInputStream("test".getBytes(), 0, 4));

        new Expectations(JarInputStream.class){};

        scanner1.close();

        scanner2.close();
        scanner2.close();

        scanner3.close();

        new Verifications() {{
            stream.close();
            times = 3;
        }};
    }

    /**
     * Reproducer for OWLS-19790: When we iterate through the all entries provided by a scanner JAR input stream should be closed.
     */
    @Tested
    PackageNamesScanner scanner = new PackageNamesScanner(new String[]{"javax.ws.rs"}, false);
    @Test
    public void testInputStreamClosedAfterIteration(@Injectable("false") boolean recursive) throws Exception {
        JarInputStream stream = new JarInputStream(
                new ByteArrayInputStream("test".getBytes(), 0, 4));

        new Expectations(JarInputStream.class) {{
            stream.getNextJarEntry();
            result = null;
            stream.close();
        }};

        while (scanner.hasNext()) {
            scanner.next();
        }
    }

    private ClassLoader createTestClassLoader(final String scheme,
                                              final URLStreamHandler urlStreamHandler,
                                              final String resourceFilePath) {
        return new ClassLoader() {
            public Enumeration<URL> getResources(final String name) throws IOException {
                final List<URL> list = new ArrayList<>();
                list.add((urlStreamHandler == null
                                  ? new URL(null, scheme + ":" + resourceFilePath + "!/" + name)
                                  : new URL(null, scheme + ":" + resourceFilePath + "!/" + name, urlStreamHandler)));
                return new Vector<>(list).elements();
            }
        };
    }

    // URLStreamHandler creation for the various schemes without having to add them as dependencies
    private URLStreamHandler createTestURLStreamHandler(final String scheme) {
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL u) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void parseURL(final URL u, final String spec, final int start, final int limit) {
                setURL(u, scheme, "", -1, null, null, spec.substring(scheme.length() + 1), null, null);
            }
        };
    }
}
