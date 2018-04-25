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

package org.glassfish.jersey.inject.hk2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.util.ExtendedLogger;
import org.glassfish.jersey.internal.util.LazyUid;
import org.glassfish.jersey.process.internal.RequestScope;

import static org.glassfish.jersey.internal.guava.Preconditions.checkState;

public class Hk2RequestScope extends RequestScope {

    @Override
    public org.glassfish.jersey.process.internal.RequestContext createContext() {
        return new Instance();
    }

    /**
     * Implementation of the request scope instance.
     */
    public static final class Instance implements org.glassfish.jersey.process.internal.RequestContext {

        private final ExtendedLogger logger = new ExtendedLogger(Logger.getLogger(Instance.class.getName()), Level.FINEST);

        /*
         * Scope instance UUID.
         *
         * For performance reasons, it's only generated if toString() method is invoked,
         * e.g. as part of some low-level logging.
         */
        private final LazyUid id = new LazyUid();

        /**
         * A map of injectable instances in this scope.
         */
        private final Map<ForeignDescriptor, Object> store;

        /**
         * Holds the number of snapshots of this scope.
         */
        private final AtomicInteger referenceCounter;

        private Instance() {
            this.store = new HashMap<>();
            this.referenceCounter = new AtomicInteger(1);
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
        public Hk2RequestScope.Instance getReference() {
            // TODO: replace counter with a phantom reference + reference queue-based solution
            referenceCounter.incrementAndGet();
            return this;
        }

        /**
         * Get an inhabitant stored in the scope instance that matches the active descriptor .
         *
         * @param <T>        inhabitant type.
         * @param descriptor inhabitant descriptor.
         * @return matched inhabitant stored in the scope instance or {@code null} if not matched.
         */
        @SuppressWarnings("unchecked")
        public <T> T get(ForeignDescriptor descriptor) {
            return (T) store.get(descriptor);
        }

        /**
         * Store a new inhabitant for the given descriptor.
         *
         * @param <T>        inhabitant type.
         * @param descriptor inhabitant descriptor.
         * @param value      inhabitant value.
         * @return old inhabitant previously stored for the given descriptor or
         * {@code null} if none stored.
         */
        @SuppressWarnings("unchecked")
        public <T> T put(ForeignDescriptor descriptor, T value) {
            checkState(!store.containsKey(descriptor),
                    "An instance for the descriptor %s was already seeded in this scope. Old instance: %s New instance: %s",
                    descriptor,
                    store.get(descriptor),
                    value);

            return (T) store.put(descriptor, value);
        }

        /**
         * Remove a value for the descriptor if present in the scope instance store.
         *
         * @param descriptor key for the value to be removed.
         */
        @SuppressWarnings("unchecked")
        public <T> void remove(ForeignDescriptor descriptor) {
            final T removed = (T) store.remove(descriptor);
            if (removed != null) {
                descriptor.dispose(removed);
            }
        }

        public boolean contains(ForeignDescriptor provider) {
            return store.containsKey(provider);
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
                    new HashSet<>(store.keySet()).forEach(this::remove);
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
}

