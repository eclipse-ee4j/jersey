/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate.virtual;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * {@link Executors} facade to support virtual threads.
 */
public interface LoomishExecutors {
    /**
     * Creates a thread pool that creates new threads as needed and uses virtual threads if available.
     * @return the newly created thread pool
     */
    ExecutorService newCachedThreadPool();

    /**
     * Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue
     * and uses virtual threads if available
     * @param nThreads – the number of threads in the pool
     * @return the newly created thread pool
     */
    ExecutorService newFixedThreadPool(int nThreads);

    /**
     * Returns thread factory used to create new threads
     * @return thread factory used to create new threads
     * @see Executors#defaultThreadFactory()
     */
    ThreadFactory getThreadFactory();

    /**
     * Return true if the virtual thread use is requested.
     * @return whether the virtual thread use is requested.
     */
    boolean isVirtual();
}
