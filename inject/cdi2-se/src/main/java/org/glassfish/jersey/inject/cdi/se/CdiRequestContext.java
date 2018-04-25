/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.cdi.se;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.util.ExtendedLogger;
import org.glassfish.jersey.internal.util.LazyUid;
import org.glassfish.jersey.process.internal.RequestContext;

/**
 * Implementation of the request context.
 */
public final class CdiRequestContext implements RequestContext {

    private static final ExtendedLogger logger =
            new ExtendedLogger(Logger.getLogger(CdiRequestContext.class.getName()), Level.FINEST);

    /*
     * Scope instance UUID.
     *
     * For performance reasons, it's only generated if toString() method is invoked,
     * e.g. as part of some low-level logging.
     */
    private final LazyUid id = new LazyUid();

    /**
     * Holds the number of snapshots of this scope.
     */
    private final AtomicInteger referenceCounter;

    /**
     * A map of injectable instances in this scope.
     */
    private final Map<String, Object> store;

    CdiRequestContext() {
        this.store = new HashMap<>();
        this.referenceCounter = new AtomicInteger(1);
    }

    Map<String, Object> getStore() {
        return store;
    }

    /**
     * Get a "new" reference of the scope instance. This will increase
     * the internal reference counter which prevents the scope instance
     * to be destroyed until a {@link #release()} method is explicitly
     * called (once per each {@code getReference()} method call).
     *
     * @return referenced scope instance.
     */
    @Override
    public RequestContext getReference() {
        // TODO: replace counter with a phantom reference + reference queue-based solution
        referenceCounter.incrementAndGet();
        return this;
    }

    /**
     * Release a single reference to the current request scope instance.
     * <p>
     * Once all instance references are released, the instance will be recycled.
     */
    @Override
    public void release() {
        if (referenceCounter.decrementAndGet() < 1) {
            try {
                store.clear();
            } finally {
                logger.debugLog("Released scope instance {0}", this);
            }
        }
    }

    @Override
    public String toString() {
        return "Instance{"
                + "id=" + id
                + ", referenceCounter=" + referenceCounter
                + ", store size=" + store.size()
                + '}';
    }
}
