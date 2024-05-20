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

package org.glassfish.jersey.innate;

import org.glassfish.jersey.innate.virtual.LoomishExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Utility class for the virtual thread support.
 */
public final class VirtualThreadSupport {

    private static final LoomishExecutors VIRTUAL_THREADS = new Java21LoomishExecutors(Thread.ofVirtual().factory());
    private static final LoomishExecutors NON_VIRTUAL_THREADS = new NonLoomishExecutors(Executors.defaultThreadFactory());

    /**
     * Do not instantiate.
     */
    private VirtualThreadSupport() {
        throw new IllegalStateException();
    }

    /**
     * Informs whether the given {@link Thread} is virtual.
     * @return true when the current thread is virtual.
     */
    public static boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    /**
     * Return an instance of {@link LoomishExecutors} based on a permission to use virtual threads.
     * @param allow whether to allow virtual threads.
     * @return the {@link LoomishExecutors} instance.
     */
    public static LoomishExecutors allowVirtual(boolean allow) {
        return allow ? VIRTUAL_THREADS : NON_VIRTUAL_THREADS;
    }

    /**
     * Return an instance of {@link LoomishExecutors} based on a permission to use virtual threads.
     * @param allow whether to allow virtual threads.
     * @param threadFactory the thread factory to be used by a the {@link ExecutorService}.
     * @return the {@link LoomishExecutors} instance.
     */
    public static LoomishExecutors allowVirtual(boolean allow, ThreadFactory threadFactory) {
        return allow ? new Java21LoomishExecutors(threadFactory) : new NonLoomishExecutors(threadFactory);
    }

    private static class NonLoomishExecutors implements LoomishExecutors {
        private final ThreadFactory threadFactory;

        private NonLoomishExecutors(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
        }

        @Override
        public ExecutorService newCachedThreadPool() {
            return Executors.newCachedThreadPool(getThreadFactory());
        }

        @Override
        public ExecutorService newFixedThreadPool(int nThreads) {
            return Executors.newFixedThreadPool(nThreads, getThreadFactory());
        }

        @Override
        public ThreadFactory getThreadFactory() {
            return threadFactory;
        }

        @Override
        public boolean isVirtual() {
            return false;
        }
    }

    private static class Java21LoomishExecutors implements LoomishExecutors {
        private final ThreadFactory threadFactory;

        private Java21LoomishExecutors(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
        }

        @Override
        public ExecutorService newCachedThreadPool() {
            return Executors.newThreadPerTaskExecutor(getThreadFactory());
        }

        @Override
        public ExecutorService newFixedThreadPool(int nThreads) {
            ThreadFactory threadFactory = this == VIRTUAL_THREADS ? Executors.defaultThreadFactory() : getThreadFactory();
            return Executors.newFixedThreadPool(nThreads, threadFactory);
        }

        @Override
        public ThreadFactory getThreadFactory() {
            return threadFactory;
        }

        @Override
        public boolean isVirtual() {
            return true;
        }
    }
}
