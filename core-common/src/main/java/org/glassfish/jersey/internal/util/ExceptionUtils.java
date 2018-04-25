/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Exception Utils class provide utility method for exception handling.
 *
 * @author Stepan Vavra (stepan.vavra@oracle.com)
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * Gets the stack trace of the provided throwable as a string.
     *
     * @param t the exception to get the stack trace for.
     * @return the stack trace as a string.
     */
    public static String exceptionStackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Based on the rethrow parameter, either rethrows the supplied exception or logs the provided message at the given level.
     *
     * @param e       the exception to rethrow if rethrow is {@code true}.
     * @param rethrow whether to rethrow an exception or just log the provided message.
     * @param logger  the logger to print the message with.
     * @param m       the message to log if rethrow is {@code false}.
     * @param level   the level of the logged message.
     * @param <T>     the type of the exception to be conditionally rethrown.
     * @throws T if rethrow is {@code true}.
     */
    public static <T extends Exception> void conditionallyReThrow(T e, boolean rethrow, Logger logger, String m, Level level)
            throws T {
        if (rethrow) {
            throw e;
        } else {
            // do not mask the other exception, just log this one
            logger.log(level, m, e);
        }
    }
}
