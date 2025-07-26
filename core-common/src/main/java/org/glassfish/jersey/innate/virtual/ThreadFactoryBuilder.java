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

package org.glassfish.jersey.innate.virtual;

/**
 * Bearer of the information used for building {@code ThreadFactory} either by using virtual threads or platform threads.
 */
public class ThreadFactoryBuilder {
    private long start = 0L;
    private String prefix = null;

    /**
     * Get the thread name prefix.
     * @return the thread name prefix.
     */
    public String prefix() {
        return prefix;
    }

    /**
     * The prefix of the name of the thread. For instance "my-factory-thread-".
     * @param prefix the thread name prefix.
     * @return
     */
    public ThreadFactoryBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Get the initial id for the first thread created by the thread factory.
     * @return the initial thread id.
     */
    public long start() {
        return start;
    }

    /**
     * Set the initial id for the first thread created by the thread factory. The prefix and the id make the name of the thread.
     * For instance "my-factory-thread-0".
     * @param start the initial id for the first thread created by the thread factory.
     * @return the updated builder.
     */
    public ThreadFactoryBuilder start(long start) {
        this.start = start;
        return this;
    }
}
