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

package org.glassfish.jersey.message.internal;

import java.io.File;
import java.io.IOException;

/**
 * Utility class.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class Utils {

    /**
     * Throws an IllegalArgumentException with given error message if the first, toCheck,
     * parameter is {@code null}.
     *
     * @param toCheck an instance to check.
     * @param errorMessage message to set to the IllegalArgumentException thrown.
     */
    static void throwIllegalArgumentExceptionIfNull(final Object toCheck, final String errorMessage) {
        if (toCheck == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Create an empty file in the default temporary-file directory.
     *
     * @return An abstract pathname denoting a newly-created empty file.
     * @throws IOException if a file could not be created.
     */
    public static File createTempFile() throws IOException {
        final File file = File.createTempFile("rep", "tmp");
        // Make sure the file is deleted when JVM is shutdown at last.
        file.deleteOnExit();
        return file;
    }

    /**
     * Prevent instantiation.
     */
    private Utils() {
        throw new AssertionError("No instances allowed.");
    }
}
