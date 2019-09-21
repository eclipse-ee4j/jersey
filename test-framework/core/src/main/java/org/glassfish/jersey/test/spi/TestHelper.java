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

package org.glassfish.jersey.test.spi;

import java.net.URI;

/**
 * Helper class for Jersey Test Framework.
 *
 * @author Michal Gajdos
 */
public final class TestHelper {

    /**
     * Create a human readable string from given URI. This method replaces {@code 0} port (start container at first available
     * port) in given URI with string {@code <AVAILABLE-PORT>}.
     *
     * @param uri an URI.
     * @return stringified URI.
     */
    public static String zeroPortToAvailablePort(final URI uri) {
        return uri.getPort() != 0
                ? uri.toString()
                : uri.toString().replaceFirst(":0", ":<AVAILABLE-PORT>");
    }

    /**
     * Prevent instantiation.
     */
    private TestHelper() {
    }
}
