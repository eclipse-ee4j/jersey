/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util.collection;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.util.JdkVersion;

/**
 * Utility class, which tries to pickup the best collection implementation depending
 * on running environment.
 *
 * @author Gustav Trede
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.3
 */
public final class DataStructures {

    private static final Class<?> LTQ_CLASS;

    static {
        String className = null;

        Class<?> c;
        try {
            final JdkVersion jdkVersion = JdkVersion.getJdkVersion();
            final JdkVersion minimumVersion = JdkVersion.parseVersion("1.7.0");

            className = (minimumVersion.compareTo(jdkVersion) <= 0)
                    ? "java.util.concurrent.LinkedTransferQueue"
                    : "org.glassfish.jersey.internal.util.collection.LinkedTransferQueue";

            c = getAndVerify(className);
            Logger.getLogger(DataStructures.class.getName()).log(Level.FINE, "USING LTQ class:{0}", c);
        } catch (final Throwable t) {
            Logger.getLogger(DataStructures.class.getName()).log(Level.FINE,
                    "failed loading data structure class:" + className
                            + " fallback to embedded one", t);

            c = LinkedBlockingQueue.class; // fallback to LinkedBlockingQueue
        }

        LTQ_CLASS = c;
    }

    /**
     * Default concurrency level calculated based on the number of available CPUs.
     */
    public static final int DEFAULT_CONCURENCY_LEVEL = ceilingNextPowerOfTwo(Runtime.getRuntime().availableProcessors());

    private static int ceilingNextPowerOfTwo(final int x) {
        // Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    private static Class<?> getAndVerify(final String cn) throws Throwable {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                @Override
                public Class<?> run() throws Exception {
                    return DataStructures.class.getClassLoader().loadClass(cn).newInstance().getClass();
                }
            });
        } catch (final PrivilegedActionException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Create an instance of a {@link BlockingQueue} that is based on
     * {@code LinkedTransferQueue} implementation from JDK 7.
     * <p>
     * When running on JDK 7 or higher, JDK {@code LinkedTransferQueue} implementation is used,
     * on JDK 6 an internal Jersey implementation class is used.
     * </p>
     *
     * @param <E> the type of elements held in the queue.
     * @return new instance of a {@link BlockingQueue} that is based on {@code LinkedTransferQueue}
     *         implementation from JDK 7.
     */
    @SuppressWarnings("unchecked")
    public static <E> BlockingQueue<E> createLinkedTransferQueue() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<BlockingQueue<E>>() {
                @Override
                public BlockingQueue<E> run() throws Exception {
                    return (BlockingQueue<E>) LTQ_CLASS.newInstance();
                }
            });
        } catch (final PrivilegedActionException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }
}
