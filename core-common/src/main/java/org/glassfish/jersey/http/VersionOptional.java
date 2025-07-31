/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.http;

/**
 * Provides information about optionality of version in {@code Cookie} and {@code NewCookie}.
 * The version is as follows: 0 - Netscape, 1 - RFC 2109 & 2965, not set RFC 6265.
 */
public interface VersionOptional {

    /**
     * The version from the original Netscape specification.
     */
    public static final int NETSCAPE_VERSION = 0;

    /**
     * Defines if the version is set.
     * @return {@code true} when the version is set.
     */
    boolean hasVersion();
}
