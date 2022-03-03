/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

public abstract class AbstractFinderTest {

    private static final String SEPARATOR = System.getProperty("path.separator");

    String setUpJaxRsApiPath() {

        final String oldClassPath = System.getProperty("java.class.path");
        final String sureFireClassPath = System.getProperty("surefire.test.class.path");
        final String modulePath = System.getProperty("jdk.module.path");
        final StringBuilder classPath = new StringBuilder();
        if (oldClassPath != null) {
            classPath.append(oldClassPath);
            classPath.append(SEPARATOR);
        }
        if (sureFireClassPath != null) {
            classPath.append(sureFireClassPath);
            classPath.append(SEPARATOR);
        }
        if (modulePath != null) {
            classPath.append(modulePath);
        }

        return parseEntries(classPath.toString());
    }

    private static String parseEntries(String fullPath) {
        final String[] entries = fullPath.split(SEPARATOR);
        for (final String entry : entries) {
            if (entry.contains("jakarta.ws.rs-api")) {
                return entry;
            }
        }
        return null;
    }

}
