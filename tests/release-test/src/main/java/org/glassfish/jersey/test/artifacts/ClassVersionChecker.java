/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.glassfish.jersey.test.artifacts;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ClassVersionChecker {
    static TestResult checkClassVersion(JarFile jar, JarEntry entry, Properties properties) throws IOException {
        final String jerseyVersion = MavenUtil.getJerseyVersion(properties);
        final int minVersion = jerseyVersion.startsWith("3.1") ? 11 : 8;
        return checkClassVersion(jar.getInputStream(entry), jar.getName() + File.separator + entry.getName(), minVersion);
    }

    private static TestResult checkClassVersion(InputStream inputStream, String filename, int version) throws IOException {
        TestResult result = new TestResult();
        DataInputStream in = new DataInputStream(inputStream);

        int magic = in.readInt();
        if (magic != -889275714) {
            result.exception().append(filename).println(" is not a valid class!");
        }

        int minor = in.readUnsignedShort();
        int major = in.readUnsignedShort();
        int classVersion = convertMajorMinorToSE(major, minor);
        TestResult.MessageBuilder builder =  classVersion <= version ? result.ok() : result.exception();
        builder.append(filename).append(": ").append(major).append(".").append(minor).append(" = JDK ")
                .println(String.valueOf(classVersion));
        in.close();
        return result;
    }

    private static int convertMajorMinorToSE(int major, int minor) {
        int comp = (major - 44 + minor);
        return comp;
    }
}
