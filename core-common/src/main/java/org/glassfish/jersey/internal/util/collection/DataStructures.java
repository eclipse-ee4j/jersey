/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Utility class, which tries to pickup the best collection implementation depending
 * on running environment.
 *
 * @author Gustav Trede
 * @author Marek Potociar
 * @since 2.3
 */
public final class DataStructures {

    /**
     * Default concurrency level calculated based on the number of available CPUs.
     */
    public static final int DEFAULT_CONCURENCY_LEVEL = ceilingNextPowerOfTwo(Runtime.getRuntime().availableProcessors());

    private static int ceilingNextPowerOfTwo(final int x) {
        // Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    /**
     * Create an instance of a {@link BlockingQueue} that is based on
     * {@code LinkedTransferQueue} implementation, available in JDK 7 and above.
     * <p>
     * Originally, the method was used to provide backwards compatibility for JDK versions 6 and below.
     * As those versions are now unsupported, callers should instantiate an {@link LinkedTransferQueue}
     * directly instead of using this method.
     * </p>
     *
     * @param <E> the type of elements held in the queue.
     * @return new instance of a {@link BlockingQueue} that is based on {@code LinkedTransferQueue}
     *         implementation from JDK 7.
     */
    @Deprecated
    public static <E> BlockingQueue<E> createLinkedTransferQueue() {
        return new LinkedTransferQueue<>();
    }

    /**
     * Creates a new, empty map with a default initial capacity (16),
     * load factor (0.75) and concurrencyLevel (16).
     * <p>
     * The method was originally used to provide the
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>, available in JDK 8 and above, for JDK 7 or earlier.
     * As those versions are now unsupported, callers should instantiate an {@link ConcurrentHashMap}
     * directly instead of using this method.
     * </p>
     *
     * @return the map.
     */
    @Deprecated
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a new map with the same mappings as the given map.
     * <p>
     * The method was originally used to provide the
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>, available in JDK 8 and above, for JDK 7 or earlier.
     * As those versions are now unsupported, callers should instantiate an {@link ConcurrentHashMap}
     * directly instead of using this method.
     * </p>
     *
     * @param map the map.
     */
    @Deprecated
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final Map<? extends K, ? extends V> map) {
        return new ConcurrentHashMap<>(map);
    }

    /**
     * Creates a new, empty map with an initial table size  accommodating the specified
     * number of elements without the need to dynamically resize.
     * <p>
     * The method was originally used to provide the
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>, available in JDK 8 and above, for JDK 7 or earlier.
     * As those versions are now unsupported, callers should instantiate an {@link ConcurrentHashMap}
     * directly instead of using this method.
     * </p>
     *
     * @param initialCapacity The implementation performs internal
     *                        sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of
     *                                  elements is negative.
     */
    @Deprecated
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Creates a new, empty map with an initial table size based on the given number of elements
     * ({@code initialCapacity}), table density ({@code loadFactor}), and number of concurrently
     * updating threads ({@code concurrencyLevel}).
     * <p>
     * The method was originally used to provide the
     * <a href="http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166edocs/jsr166e/ConcurrentHashMapV8.html">
     * {@code ConcurrentHashMapV8}</a>, available in JDK 8 and above, for JDK 7 or earlier.
     * As those versions are now unsupported, callers should instantiate an {@link ConcurrentHashMap}
     * directly instead of using this method.
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
    @Deprecated
    public static <K, V> ConcurrentMap<K, V> createConcurrentMap(
            final int initialCapacity, final float loadFactor,
            final int concurrencyLevel) {
        return new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }
}
