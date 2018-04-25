/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.process;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * Uncaught exception handler that can be used by various Jersey request processing thread pools uncaught exceptions.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.17
 */
public class JerseyProcessingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(JerseyProcessingUncaughtExceptionHandler.class.getName());

    private final Level logLevel;

    /**
     * Create new Jersey processing uncaught exception handler.
     * <p>
     * All uncaught exceptions will be logged using the {@link Level#WARNING WARNING} logging level.
     * </p>
     */
    public JerseyProcessingUncaughtExceptionHandler() {
        this(Level.WARNING);
    }

    /**
     * Create new Jersey processing uncaught exception handler.
     * <p>
     * All uncaught exceptions will be logged using the supplied logging level.
     * </p>
     *
     * @param logLevel custom logging level that should be used to log uncaught exceptions.
     */
    public JerseyProcessingUncaughtExceptionHandler(Level logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.log(logLevel, LocalizationMessages.UNHANDLED_EXCEPTION_DETECTED(t.getName()), e);
    }
}
