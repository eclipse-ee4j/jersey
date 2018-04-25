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

    /**
     * Creates a new, empty map with a default initial capacity (16),
     * load factor (0.75) and concurrencyLevel (16).
     * <p>
     * On Oracle JDK, the factory method will return an instance of
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>
     * that is supposed to be available in JDK 8 and provides better performance and memory characteristics than
     * {@link ConcurrentHashMap} implementation from JDK 7 or earlier. On non-Oracle JDK,
     * the factory instantiates the standard {@code ConcurrentHashMap} from JDK.
     * </p>
     *
     * @return the map.
     */
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap() {
        return JdkVersion.getJdkVersion().isUnsafeSupported()
                ? new ConcurrentHashMapV8<K, V>()
                : new ConcurrentHashMap<K, V>();
    }

    /**
     * Creates a new map with the same mappings as the given map.
     * <p>
     * On Oracle JDK, the factory method will return an instance of
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>
     * that is supposed to be available in JDK 8 and provides better performance and memory characteristics than
     * {@link ConcurrentHashMap} implementation from JDK 7 or earlier. On non-Oracle JDK,
     * the factory instantiates the standard {@code ConcurrentHashMap} from JDK.
     * </p>
     *
     * @param map the map.
     */
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final Map<? extends K, ? extends V> map) {
        return JdkVersion.getJdkVersion().isUnsafeSupported()
                ? new ConcurrentHashMapV8<K, V>(map)
                : new ConcurrentHashMap<K, V>(map);
    }

    /**
     * Creates a new, empty map with an initial table size  accommodating the specified
     * number of elements without the need to dynamically resize.
     * <p>
     * On Oracle JDK, the factory method will return an instance of
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>
     * that is supposed to be available in JDK 8 and provides better performance and memory characteristics than
     * {@link ConcurrentHashMap} implementation from JDK 7 or earlier. On non-Oracle JDK,
     * the factory instantiates the standard {@code ConcurrentHashMap} from JDK.
     * </p>
     *
     * @param initialCapacity The implementation performs internal
     *                        sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of
     *                                  elements is negative.
     */
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final int initialCapacity) {
        return JdkVersion.getJdkVersion().isUnsafeSupported()
                ? new ConcurrentHashMapV8<K, V>(initialCapacity)
                : new ConcurrentHashMap<K, V>(initialCapacity);
    }

    /**
     * Creates a new, empty map with an initial table size based on the given number of elements
     * ({@code initialCapacity}), table density ({@code loadFactor}), and number of concurrently
     * updating threads ({@code concurrencyLevel}).
     * <p>
     * On Oracle JDK, the factory method will return an instance of
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>
     * that is supposed to be available in JDK 8 and provides better performance and memory characteristics than
     * {@link ConcurrentHashMap} implementation from JDK 7 or earlier. On non-Oracle JDK,
     * the factory instantiates the standard {@code ConcurrentHashMap} from JDK.
     * </p>
     *
     * @param initialCapacity  the initial capacity. The implementation
     *                         performs internal sizing to accommodate this many elements,
     *                         given the specified load factor.
     * @param loadFactor       the load factor (table density) for
     *                         establishing the initial table size.
     * @param concurrencyLevel the estimated number of concurrently
     *                         updating threads. The implementation may use this value as
     *                         a sizing hint.
     * @throws IllegalArgumentException if the initial capacity is
     *                                  negative or the load factor or concurrencyLevel are
     *                                  not positive.
     */
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final int initialCapacity, final float loadFactor,
            final int concurrencyLevel) {
        return JdkVersion.getJdkVersion().isUnsafeSupported()
                ? new ConcurrentHashMapV8<K, V>(initialCapacity, loadFactor, concurrencyLevel)
                : new ConcurrentHashMap<K, V>(initialCapacity, loadFactor, concurrencyLevel);
    }
}
