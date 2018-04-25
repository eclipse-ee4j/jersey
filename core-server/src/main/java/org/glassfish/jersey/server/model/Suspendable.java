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

package org.glassfish.jersey.server.model;

import java.util.concurrent.TimeUnit;

/**
 * Jersey model component that is suspendable and may hold suspend-related
 * information.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface Suspendable {

    /**
     * Check if the component is marked for suspending.
     *
     * @return {@code true} if the component is marked for suspending,
     *     {@code false} otherwise.
     */
    public boolean isSuspendDeclared();

    /**
     * Check if the component is marked to be executed asynchronously by using
     * an internal Jersey {@link java.util.concurrent.ExecutorService executor service}.
     *
     * @return {@code true} if the component is marked for managed asynchronous execution,
     *     {@code false} otherwise.
     */
    public boolean isManagedAsyncDeclared();

    /**
     * Get the suspend timeout value in the given {@link #getSuspendTimeoutUnit()
     * time unit}.
     *
     * @return suspend timeout value.
     */
    public long getSuspendTimeout();

    /**
     * Get the suspend {@link #getSuspendTimeout() timeout value} time unit.
     *
     * @return time unit of the suspend timeout value.
     */
    public TimeUnit getSuspendTimeoutUnit();
}
