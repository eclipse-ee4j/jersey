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

package org.glassfish.jersey.internal.util;

/**
 * JDK Version related utilities. Ported from Grizzly project.
 *
 * @author Ryan Lubke (ryan.lubke at oracle.com)
 * @author Alexey Stashok (oleksiy.stashok at oracle.com)
 * @since 2.3
 */
public class JdkVersion implements Comparable<JdkVersion> {

    private static final boolean IS_UNSAFE_SUPPORTED;

    static {
        boolean unsafeSupported;

        try {
            // Look for sun.misc.Unsafe.
            unsafeSupported = Class.forName("sun.misc.Unsafe") != null;

            // Check environment availability.
            // Google App Engine (see https://developers.google.com/appengine/docs/java/#Java_The_environment).
            unsafeSupported &= System.getProperty("com.google.appengine.runtime.environment") == null;
        } catch (final Throwable t) {
            // Make Unsafe not supported if either:
            // - sun.misc.Unsafe not found.
            // - we're not granted to read the property (* is not enough).
            unsafeSupported = false;
        }

        IS_UNSAFE_SUPPORTED = unsafeSupported;
    }

    private static final JdkVersion UNKNOWN_VERSION = new JdkVersion(-1, -1, -1, -1);
    private static final JdkVersion JDK_VERSION = parseVersion(System.getProperty("java.version"));

    private final int major;
    private final int minor;
    private final int maintenance;
    private final int update;

    // ------------------------------------------------------------ Constructors

    private JdkVersion(final int major, final int minor, final int maintenance, final int update) {
        this.major = major;
        this.minor = minor;
        this.maintenance = maintenance;
        this.update = update;
    }

    // ---------------------------------------------------------- Public Methods

    public static JdkVersion parseVersion(String versionString) {
        try {
            final int dashIdx = versionString.indexOf('-');
            if (dashIdx != -1) {
                versionString = versionString.substring(0, dashIdx);
            }
            final String[] parts = versionString.split("\\.|_");
            if (parts.length == 3) {
                return new JdkVersion(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        0);
            } else {
                return new JdkVersion(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]));
            }
        } catch (final Exception e) {
            return UNKNOWN_VERSION;
        }
    }

    public static JdkVersion getJdkVersion() {
        return JDK_VERSION;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMajor() {
        return major;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMinor() {
        return minor;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMaintenance() {
        return maintenance;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getUpdate() {
        return update;
    }

    /**
     * Returns <tt>true</tt> if {@code sun.misc.Unsafe} is present in the
     * current JDK version, or <tt>false</tt> otherwise.
     *
     * @since 2.3.6
     */
    public boolean isUnsafeSupported() {
        return IS_UNSAFE_SUPPORTED;
    }

    @Override
    public String toString() {
        return "JdkVersion" + "{major=" + major + ", minor=" + minor + ", maintenance=" + maintenance
                + ", update=" + update + '}';
    }

    // ------------------------------------------------- Methods from Comparable

    public int compareTo(final String versionString) {
        return compareTo(JdkVersion.parseVersion(versionString));
    }

    @Override
    public int compareTo(final JdkVersion otherVersion) {
        if (major < otherVersion.major) {
            return -1;
        }
        if (major > otherVersion.major) {
            return 1;
        }
        if (minor < otherVersion.minor) {
            return -1;
        }
        if (minor > otherVersion.minor) {
            return 1;
        }
        if (maintenance < otherVersion.maintenance) {
            return -1;
        }
        if (maintenance > otherVersion.maintenance) {
            return 1;
        }
        if (update < otherVersion.update) {
            return -1;
        }
        if (update > otherVersion.update) {
            return 1;
        }
        return 0;
    }
}
