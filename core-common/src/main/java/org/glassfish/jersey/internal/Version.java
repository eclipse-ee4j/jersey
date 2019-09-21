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

package org.glassfish.jersey.internal;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for reading build.properties file.
 *
 * @author Paul Sandoz
 */
public final class Version {

    private static String buildId;
    private static String version = null;

    static {
        _initiateProperties();
    }

    private Version() {
        throw new AssertionError("Instantiation not allowed.");
    }

    private static void _initiateProperties() {
        final InputStream in = getIntputStream();
        if (in != null) {
            try {
                final Properties p = new Properties();
                p.load(in);
                final String timestamp = p.getProperty("Build-Timestamp");
                version = p.getProperty("Build-Version");

                buildId = String.format("Jersey: %s %s", version, timestamp);
            } catch (final Exception e) {
                buildId = "Jersey";
            } finally {
                close(in);
            }
        }
    }

    private static void close(final InputStream in) {
        try {
            in.close();
        } catch (final Exception ex) {
            // Ignore
        }
    }

    private static InputStream getIntputStream() {
        try {
            return Version.class.getResourceAsStream("build.properties");
        } catch (final Exception ex) {
            return null;
        }
    }

    /**
     * Get build id.
     *
     * @return build id string. Contains version and build timestamp.
     */
    public static String getBuildId() {
        return buildId;
    }

    /**
     * Get Jersey version.
     *
     * @return Jersey version.
     */
    public static String getVersion() {
        return version;
    }
}
