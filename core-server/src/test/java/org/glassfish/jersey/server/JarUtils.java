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

package org.glassfish.jersey.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Michal Gajdos
 */
public final class JarUtils {

    public enum Suffix {
        jar, zip
    }

    public static File createJarFile(final String base, final String... entries) throws IOException {
        return createJarFile(Suffix.jar, base, entries);
    }

    public static File createJarFile(final Suffix s, final String base, final String... entries) throws IOException {
        final Map<String, String> entriesMap = new HashMap<String, String>();
        for (final String entry : entries) {
            entriesMap.put(entry, entry);
        }
        return createJarFile(s, base, entriesMap);
    }

    public static File createJarFile(final String name, final Suffix s, final String base, final String... entries)
            throws IOException {
        final Map<String, String> entriesMap = new HashMap<String, String>();
        for (final String entry : entries) {
            entriesMap.put(entry, entry);
        }
        return createJarFile(name, s, base, entriesMap);
    }

    public static File createJarFile(final Suffix s, final String base, final Map<String, String> entries) throws IOException {
        return createJarFile("test", s, base, entries);
    }

    public static File createJarFile(final String name, final Suffix s, final String base, final Map<String, String> entries)
            throws IOException {
        final File tempJar = File.createTempFile(name, "." + s);
        tempJar.deleteOnExit();
        final JarOutputStream jos = new JarOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(tempJar)), new Manifest());

        final Set<String> usedSegments = new HashSet<String>();
        for (final Map.Entry<String, String> entry : entries.entrySet()) {
            for (final String path : getPaths(entry.getValue())) {
                if (usedSegments.contains(path)) {
                    continue;
                }

                usedSegments.add(path);
                final JarEntry e = new JarEntry(path);
                jos.putNextEntry(e);
                jos.closeEntry();
            }

            final JarEntry e = new JarEntry(entry.getValue());
            jos.putNextEntry(e);

            final InputStream f = new BufferedInputStream(
                    new FileInputStream(base + entry.getKey()));
            final byte[] buf = new byte[1024];
            int read = 1024;
            while ((read = f.read(buf, 0, read)) != -1) {
                jos.write(buf, 0, read);
            }
            jos.closeEntry();
        }

        jos.close();
        return tempJar;
    }

    private static String[] getPaths(final String entry) {
        final String[] segments = entry.split("/");
        final String[] paths = new String[segments.length - 1];

        if (paths.length == 0) {
            return paths;
        }

        paths[0] = segments[0] + "/";
        for (int i = 1; i < paths.length; i++) {
            paths[i] = "";
            for (int j = 0; j <= i; j++) {
                paths[i] += segments[j] + "/";
            }
        }

        return paths;
    }

    /**
     * Prevent instantiation.
     */
    private JarUtils() {
    }
}
