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

package org.glassfish.jersey.server.internal.process;

import java.io.Closeable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Default implementation of {@link CloseableService}.
 *
 * This implementation stores instances of {@code Closeable} in an internal identity hash set and makes sure
 * that the close method is invoked at most once.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class DefaultCloseableService implements CloseableService {

    private static final Logger LOGGER = Logger.getLogger(DefaultCloseableService.class.getName());

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Set<Closeable> closeables = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public boolean add(final Closeable closeable) {
        return !closed.get() && closeables.add(closeable);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            for (final Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                            LocalizationMessages.CLOSEABLE_UNABLE_TO_CLOSE(closeable.getClass().getName()), ex);
                }
            }
        }
    }
}
