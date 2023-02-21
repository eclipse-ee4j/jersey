/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.innate.inject;

import org.glassfish.jersey.internal.util.ExtendedLogger;
import org.glassfish.jersey.internal.util.LazyUid;
import org.glassfish.jersey.process.internal.RequestScope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NonInjectionRequestScope extends RequestScope {
    @Override
    public org.glassfish.jersey.process.internal.RequestContext createContext() {
        return new Instance();
    }

    /**
     * Implementation of the request scope instance.
     */
    public static final class Instance implements org.glassfish.jersey.process.internal.RequestContext {

        private static final ExtendedLogger logger = new ExtendedLogger(Logger.getLogger(Instance.class.getName()), Level.FINEST);

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

        private Instance() {
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
        public NonInjectionRequestScope.Instance getReference() {
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
            referenceCounter.decrementAndGet();
        }

        @Override
        public String toString() {
            return "Instance{"
                    + "id=" + id
                    + ", referenceCounter=" + referenceCounter
                    + '}';
        }
    }
}
