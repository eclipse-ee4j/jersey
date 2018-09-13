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

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.jersey.server.ResourceFinder;

import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Martin Snyder
 * @author Jason T. Greene
 */
public class VFSSchemeResourceFinderTest {

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

    /**
     * Test case for JERSEY-2197, JERSEY-2175.
     */
    @Test
    public void testClassEnumeration() throws Exception {
        // Count actual entries.

        int actualEntries = 0;
        try (JarFile jarFile = new JarFile(jaxRsApiPath)) {
            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class") && !entry.getName().endsWith("module-info.class")) {
                    actualEntries++;
                }
            }
        }

        // Scan entries using VFS scanner.
        final VirtualFile mountDir = VFS.getChild("content");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try (TempFileProvider provider = TempFileProvider.create("test", executor, false);
             Closeable mount = VFS.mountZip(VFS.getChild(jaxRsApiPath), mountDir, provider)) {

            ResourceFinder finder = new VfsSchemeResourceFinderFactory()
                    .create(new URI(mountDir.toURI().toString() + "/javax/ws/rs"), true);

            int scannedEntryCount = 0;
            while (finder.hasNext()) {
                // Fetch next entry.
                finder.next();

                // This test doesn't actually do anything with the input stream, but it is important that it
                // open/close the stream to simulate actual usage.  The reported defect is only exposed if you
                // call open/close in some fashion.

                try (InputStream classStream = finder.open()) {
                    scannedEntryCount++;
                }
            }

            assertThat("Failed to enumerate all contents of javax.ws.rs-api.", scannedEntryCount, equalTo(actualEntries));
        } finally {
            executor.shutdownNow();
        }
    }
}
